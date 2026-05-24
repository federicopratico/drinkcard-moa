# Functional Documentation for the Festival App

## 1. General Overview

DrinkCard MOA is an application for digitizing a festival drink card. It replaces the physical paper card with a digital account containing credits, online payments, and single-use QR tickets that can be redeemed for drinks at the bar.

The application is divided into three main experiences:

- Volunteer experience: registration, login, balance lookup, credit purchase, and QR generation to request drinks.
- Bar experience: QR scanning, ticket validation, and credit consumption.
- Organizer experience: administration panel for checking users, drink accounts, payments, and operational metrics.

The backend is built with Spring Boot and exposes a JWT-authenticated REST API. The frontend is built with React, TypeScript, and Vite.

## 2. Users and Roles

### Volunteer

This is the main user of the mobile app. Volunteers can create an account, log in, check their digital card, buy credits, and generate a QR code to redeem a drink.

When a volunteer registers, the system also creates the associated DrinkCard account. That account starts with 0 credits and `ACTIVE` status.

### Bar Staff

Bar staff use the scanning screen to read QR tickets and approve drink consumption. In the current state of the application, the scanning screen is available to authenticated users. The model includes the `BAR_STAFF` role, but role-specific restrictions have not yet been applied to the routes or to the consumption endpoint.

### Administrator

Administrators have access to the organizer panel at `/admin`. They can view registered users, DrinkCard accounts, balances, and payments. Administrative endpoints are protected with the `ADMIN` role.

The project includes an initial administrator user:

- Email: `admin@drinkcard.local`
- Role: `ADMIN`

## 3. Access, Registration, and Session

### User Registration

Frontend route: `/register`

The form asks for:

- First name
- Last name
- Email
- Password

Main rules:

- The email must have a valid format.
- The password must have at least 8 characters.
- The email cannot already exist.
- The user is created with the `VOLUNTEER` role.
- The user is created with `ACTIVE` status.
- On registration, an internal `UserRegisteredEvent` is published.
- The DrinkCard module listens to that event and creates a drink account associated with the user.

Endpoint:

```http
POST /api/v1/auth/register
```

### Login

Frontend route: `/login`

The user logs in with email and password. If the credentials are correct and the user is active, the backend returns a session containing:

- JWT token
- User/volunteer ID
- Email
- Role

The session is stored in `localStorage` under the `drinkcard.session` key, which keeps the session active after reloading the page.

Endpoint:

```http
POST /api/v1/auth/login
```

### Logout

The logout button clears the local session and forces the user to log in again before accessing protected routes.

### Protected Routes

The `/app`, `/bar/scanner`, and `/admin` routes require an authenticated session. If no session exists, the frontend redirects to `/login`.

The `/admin` route requires the `ADMIN` role; if a user without that role tries to enter, they are redirected to `/app`.

## 4. Volunteer Experience

### Main Card Screen

Frontend route: `/app`

This screen shows the current state of the volunteer's digital card:

- Available credits.
- Volunteer name.
- DrinkCard account status.
- Last confirmed purchase.
- Main actions: buy credits and generate a QR code to request a drink.

The screen queries two backend resources:

```http
GET /api/v1/drink-card-accounts/me
GET /api/v1/users/me
```

The DrinkCard account returns:

- `volunteerId`
- `credits`
- `status`
- `lastPurchaseTimestamp`

### Buying Credits

From the main screen, the volunteer can buy a card with 5 drinks.

Business rules:

- Each purchase adds 5 credits.
- The card price is 10 EUR.
- The purchase is handled through SumUp.
- The system generates an idempotency key for each checkout to avoid duplicates.
- If a payment with the same idempotency key already exists, the existing payment is returned.
- The account must exist.
- The account must be active.
- More than one purchase per day is not allowed for the same account.

Flow:

1. The user clicks `Buy 5 drinks`.
2. The frontend asks the backend to create a payment checkout.
3. The backend creates a `PENDING` payment.
4. The backend requests a hosted checkout from SumUp.
5. The frontend displays a link to open the SumUp checkout.
6. When returning to the app through the success URL, the payment is confirmed.
7. If SumUp reports a successful payment, the payment changes to `SUCCESS` and 5 credits are added to the account.
8. If SumUp reports a failure or expiration, the payment changes to `FAILED` or `EXPIRED`.

Endpoints:

```http
POST /api/v1/payments/checkout
POST /api/v1/payments/{paymentId}/confirm
```

Payment statuses:

- `PENDING`: payment created, waiting for confirmation.
- `SUCCESS`: payment confirmed and credits added.
- `FAILED`: failed payment.
- `EXPIRED`: expired payment.

### Payment Return

Frontend route: `/payment/success`

This screen opens when the user returns from the payment provider. The app retrieves the pending payment stored in the browser, calls the backend to confirm the real payment status, and displays the result:

- Confirmed payment: shows the current credits.
- Pending, failed, or expired payment: informs the user that credits were not added.
- No pending payment in the browser: shows a warning.

## 5. Drink Selection and QR

### Drink Selection

Frontend route: `/app/drinks`

The volunteer can choose a drink to generate a QR ticket. The options visible in the frontend are:

- Beer
- Wine
- Water
- Soft drink

Each drink costs 1 credit.

When a drink is selected, the frontend creates a drink ticket in the backend.

Endpoint:

```http
POST /api/v1/drink-tickets
```

Rules for creating a ticket:

- The user must have a DrinkCard account.
- The account must be active.
- The account must have at least 1 credit.
- There cannot be another active pending ticket for the same volunteer.
- The ticket is created with `PENDING` status.
- The ticket expires after 90 seconds.

The QR does not contain sensitive information such as balance, email, or user type. It only contains the ticket identifier:

```json
{
  "ticketId": "TICKET_UUID"
}
```

### QR Screen

Frontend route: `/app/qr`

The screen shows:

- Selected drink name.
- QR code.
- Countdown until expiration.
- Single-use ticket indicator.
- Ticket ID.
- Current ticket status.

The screen queries the ticket status every 3 seconds:

```http
GET /api/v1/drink-tickets/{ticketId}/status
```

Ticket statuses:

- `PENDING`: valid ticket, waiting to be scanned.
- `CONSUMED`: ticket already used.
- `EXPIRED`: expired ticket.

If the ticket is consumed or expires, the QR is shown as inactive.

### Automatic Ticket Expiration

The backend includes a scheduled task that looks for expired `PENDING` tickets and marks them as `EXPIRED`.

Current configuration:

```yaml
drinkcard:
  ticket-expiration-cleanup-delay-ms: 60000
```

This means the cleanup process runs every 60 seconds by default.

## 6. Bar Experience

### QR Scanner

Frontend route: `/bar/scanner`

The bar screen can consume tickets in two ways:

- Camera scanning using `html5-qrcode`.
- Manual consumption by entering the ticket ID or pasting the QR payload.

Flow:

1. The screen tries to activate the device's rear camera.
2. If it cannot use the rear camera, it looks for an available camera.
3. If there is no camera or the browser blocks permissions, the ticket can be entered manually.
4. When reading the QR, it extracts the `ticketId`.
5. It calls the backend to consume the ticket.
6. If the ticket is valid, it shows the approved drink and remaining balance.
7. If the ticket does not exist, is expired, has already been consumed, or is not valid, it shows a rejection.

Endpoint:

```http
POST /api/v1/drink-tickets/{ticketId}/consume
```

Consumption rules:

- The ticket must exist.
- The ticket must be in `PENDING` status.
- The ticket must not be expired.
- The associated DrinkCard account must exist.
- The account must have enough credits.
- On consumption, the ticket changes to `CONSUMED`.
- On consumption, 1 credit is deducted from the account.
- The identifier of the user who performed the consumption is stored as `consumedByStaffId`.

Expected response:

- Ticket ID.
- Final status.
- Drink type.
- Remaining credits.

## 7. Organizer Panel

Base frontend route: `/admin`

The organizer panel uses a desktop layout with side navigation. It includes:

- Dashboard.
- Volunteers.
- Shifts.
- Analytics.

Only users with the `ADMIN` role can access it from the frontend and from the administrative endpoints.

### Dashboard

Frontend route: `/admin`

The dashboard summarizes the main activity:

- Active cards.
- Available credits across all accounts.
- Volunteer users.
- Confirmed payments.
- Confirmed revenue.
- Recent payment activity.
- Drink mix shown as a simple chart.

Real data loaded from the backend:

```http
GET /api/v1/admin/users
GET /api/v1/admin/drink-card-accounts
GET /api/v1/admin/payments
```

Current status note:

- User, account, and payment metrics come from the backend.
- The `Drink mix` block uses static frontend data; it is not yet connected to a real analytics endpoint.

### Volunteer Management and Lookup

Frontend route: `/admin/volunteers`

Allows the administrator to view volunteers and their DrinkCard accounts.

Features:

- List users with the `VOLUNTEER` role.
- List DrinkCard accounts.
- Match user and account by volunteer ID.
- Search by name, email, status, ID, or account status.
- Refresh data.
- Select a volunteer to view details.

Displayed detail:

- Name.
- Email.
- User status.
- Account status.
- Available credits.
- Last purchase.

Endpoints:

```http
GET /api/v1/admin/users?role=VOLUNTEER
GET /api/v1/admin/users/{userId}
GET /api/v1/admin/drink-card-accounts
GET /api/v1/admin/drink-card-accounts/{volunteerId}
```

The user list supports filters and pagination:

- `role`
- `status`
- `email`
- `page`
- `size`
- `sort`

### Bar Shifts

Frontend route: `/admin/shifts`

The screen shows basic shift planning:

- Schedule.
- Assigned bar.
- Number of volunteers.
- Shift status.

Current status:

- Shifts are defined as static frontend data.
- The `New shift` button exists visually, but it does not create shifts yet.
- There are no connected backend endpoints for real shift management.

### Analytics

Frontend route: `/admin/analytics`

The screen shows a chart of drink consumption by hour and operational notes.

Current status:

- The chart uses static data.
- The frontend itself indicates that the real-time endpoint is pending.
- Real analytics should be built from consumed tickets, payments, and future transaction events.

## 8. Administrative Backend Features

### Users

Endpoints:

```http
GET /api/v1/admin/users
GET /api/v1/admin/users/{userId}
```

Features:

- List users.
- Filter by role, status, or email.
- Pagination.
- Sorting.
- View user details.

Protection:

- Requires JWT.
- Requires the `ADMIN` role.

### DrinkCard Accounts

Endpoints:

```http
GET /api/v1/admin/drink-card-accounts
GET /api/v1/admin/drink-card-accounts/{volunteerId}
```

Features:

- List drink accounts.
- Find account by volunteer ID.
- View credits, status, and last purchase.

Protection:

- Requires JWT.
- Requires the `ADMIN` role.

### Payments

Endpoint:

```http
GET /api/v1/admin/payments
```

Supported filters:

- `volunteerId`
- `status`
- `from`
- `to`
- `page`
- `size`
- `sort`

Features:

- Review volunteer payments.
- Filter by volunteer.
- Filter by status.
- Filter by time range.
- Sort and paginate results.

Protection:

- Requires JWT.
- Requires the `ADMIN` role.

## 9. Main Business Rules

### Credits

- Each purchased card adds 5 credits.
- Each consumed drink deducts 1 credit.
- A ticket cannot be generated if the account has no credits.
- The balance lives in the DrinkCard account, not in the QR.

### Purchase

- The card price is 10 EUR.
- Only one purchase per day is allowed for an account.
- Only an active account can buy.
- Real confirmation depends on the status returned by SumUp.

### QR Ticket

- The ticket expires after 90 seconds.
- The QR is single-use.
- Only one active pending ticket can exist per volunteer.
- The ticket can be consumed, expired, or remain pending.
- The QR only contains the ticket ID.

### Security

- Authentication is based on JWT.
- Registration and login are public.
- All other endpoints require authentication.
- Administrative endpoints require the `ADMIN` role.
- The backend is stateless: it does not use server sessions.

### Statuses

Users:

- `ACTIVE`
- `SUSPENDED`
- `DELETED`

DrinkCard accounts:

- `ACTIVE`
- Other domain statuses can be considered for suspension, although the current interface only reads the status.

Payments:

- `PENDING`
- `SUCCESS`
- `FAILED`
- `EXPIRED`

Tickets:

- `PENDING`
- `CONSUMED`
- `EXPIRED`

## 10. External Integrations

### SumUp

The app uses SumUp to create payment checkouts and query checkout status.

Main configuration:

- `SUMUP_BASE_URL`
- `SUMUP_API_KEY`
- `SUMUP_MERCHANT_CODE`
- `PAYMENT_FRONTEND_SUCCESS_URL`

The backend creates a hosted checkout with:

- Payment reference.
- Amount.
- `EUR` currency.
- `Drink card - 5 credits` description.
- Return URL to the frontend.

### Database

The database uses Flyway migrations.

Main tables:

- `users`: users, credentials, roles, and status.
- `drink_card_accounts`: digital card balance and status.
- `payments`: payments and provider data.
- `drink_tickets`: QR tickets, status, expiration, and consumption.

## 11. Functional Limitations and Pending Work

These parts exist visually or are planned, but they are not complete as real functionality:

- Real bar shift management: the screen exists, but it uses static data and the create-shift button does not persist information.
- Real analytics: the drink consumption by hour chart uses static data.
- Real drink mix: the dashboard shows fixed percentages, not values calculated from consumed tickets.
- Personal volunteer payment history: there is no user history screen yet.
- Personal volunteer ticket history: there is no ticket history screen yet.
- Credit transaction ledger: the balance exists, but there is no detailed historical table of credit movements.
- Strict restriction for bar staff: the scanner works for authenticated users, but it does not yet require the `BAR_STAFF` role.
- Admin actions to suspend/reactivate users or accounts: the model has statuses, but PATCH actions are not implemented in the current API.
- Admin drink ticket detail/listing endpoints: they are documented as possible future stories, but they are not exposed in the current implementation.

## 12. Main Flow Summary

### Registration Flow

1. The user registers.
2. The backend creates the `VOLUNTEER` user.
3. A user-registered event is published.
4. The associated DrinkCard account is created.
5. The user can log in.

### Purchase Flow

1. The volunteer enters `/app`.
2. They click to buy 5 drinks.
3. The backend creates a pending payment.
4. SumUp creates the checkout.
5. The volunteer pays in SumUp.
6. The app confirms the payment when the volunteer returns.
7. If the payment is successful, 5 credits are added.

### Drink Consumption Flow

1. The volunteer chooses a drink.
2. The backend creates a temporary ticket.
3. The app displays a QR with the ticket ID.
4. The bar scans the QR.
5. The backend validates the ticket.
6. If it is valid, the backend marks the ticket as consumed.
7. The backend deducts 1 credit.
8. The bar sees the approval and the remaining balance.

### Administration Flow

1. The administrator logs in.
2. They access `/admin`.
3. They check general metrics.
4. They review volunteers and accounts.
5. They check recent and filterable payments.
6. They use the shifts and analytics screens as a visual base for future backend-connected features.
