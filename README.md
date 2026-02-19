# 📄 Contract Management System

A full-stack **Contract Management System** built with **Spring Boot** and **Thymeleaf**, featuring a modern Vercel-inspired dark UI. Manage contracts end-to-end with role-based access, file attachments, email notifications, advanced filtering, and visual analytics.

---

## ✨ Features

### 🔐 Authentication & Security
- **User Registration** with role selection (Admin / User)
- **Secure Login** with session-based authentication
- **Password Hashing** using BCrypt (Spring Security Crypto)
- **Forgot Password** flow with email OTP verification
- **OTP Resend** with expiry-based validation
- **Session Management** with configurable timeout (default 24h)
- **Auth Interceptor** to protect routes from unauthenticated access

### 📑 Contract Management (CRUD)
- **Create** contracts with title, description, dates, status, party, and type
- **View** detailed contract information in a dedicated page
- **Edit** existing contracts with pre-populated form data
- **Delete** contracts with confirmation
- **File Attachments** — upload documents (up to 5 MB) tied to contracts
- **File Download** — securely download attached contract documents
- **Custom Contract Types** — extend beyond default types (Service, Employment, NDA, Lease, Sales, Partnership) with user-defined types

### 📊 Dashboard
- **At-a-glance statistics** — total, active/running, and expiring contract counts
- **Contract list** with clickable rows for quick viewing
- **Advanced Filtering** by:
  - Keyword search (title)
  - Status (Draft, Signed, Running, Expired)
  - Party (Internal, External, Government, Vendor, Client)
  - Contract Type (default + custom)
  - Date range (from / to)
- **Filter persistence** — selected filters are preserved in the UI

### 📈 Reports & Analytics
- **Summary cards** with key contract metrics
- **Visual charts** for:
  - Contracts by Status
  - Contracts by Party
  - Contracts by Type (dynamic, includes custom types)

### 🔔 Notifications
- **In-app notification system** with multiple types (Info, Warning, Success, Danger)
- **Read/unread tracking** per notification
- **Mark as read** and **delete** individual notifications
- **Mark all as read** bulk action
- **Unread badge count** displayed globally in the navigation

### ⚙️ User Settings
- **Update Profile** — change full name and email
- **Change Password** — with current password verification and confirmation matching

### 👥 Admin Panel
- **User management** — view all registered users (Admin-only route)

### 🎨 UI / UX
- **Vercel-inspired dark theme** — pure black background, white accents, monochrome palette
- **Geist Sans** font for a clean, modern aesthetic
- **Responsive layout** suitable for desktop and tablet
- **Thymeleaf fragments** for reusable header/navigation components

---

## 🛠️ Tech Stack

| Layer         | Technology                          |
|---------------|-------------------------------------|
| **Backend**   | Java 17, Spring Boot 3.2.2          |
| **Frontend**  | Thymeleaf, HTML5, CSS3, JavaScript  |
| **Database**  | MySQL 8+                            |
| **ORM**       | Spring Data JPA / Hibernate         |
| **Security**  | Spring Security Crypto (BCrypt)     |
| **Email**     | Spring Boot Mail (SMTP / Gmail)     |
| **Validation**| Jakarta Bean Validation             |
| **Build**     | Apache Maven                        |
| **Dev Tools** | Spring Boot DevTools (hot reload)   |

---

## 📁 Project Structure

```
cms/
├── pom.xml
├── src/
│   └── main/
│       ├── java/com/cms/
│       │   ├── CmsApplication.java            # Entry point
│       │   ├── config/
│       │   │   ├── AuthInterceptor.java        # Route protection
│       │   │   ├── GlobalModelAdvice.java      # Global model attributes
│       │   │   └── WebConfig.java              # MVC configuration
│       │   ├── controller/
│       │   │   ├── AuthController.java         # Login, Register, OTP, Logout
│       │   │   ├── ContractController.java     # Contract CRUD + file ops
│       │   │   ├── DashboardController.java    # Dashboard + filtering
│       │   │   ├── ReportsController.java      # Reports & analytics
│       │   │   ├── NotificationController.java # Notification management
│       │   │   ├── SettingsController.java      # Profile & password
│       │   │   └── AdminController.java        # Admin user management
│       │   ├── model/
│       │   │   ├── Contract.java               # Contract entity
│       │   │   ├── User.java                   # User entity
│       │   │   └── Notification.java           # Notification entity
│       │   ├── repository/
│       │   │   ├── ContractRepository.java     # Contract data access
│       │   │   ├── ContractSpecification.java  # Dynamic query filters
│       │   │   ├── UserRepository.java         # User data access
│       │   │   └── NotificationRepository.java # Notification data access
│       │   └── service/
│       │       ├── ContractService.java        # Contract business logic
│       │       ├── ContractExpiryService.java  # Expiry detection
│       │       ├── UserService.java            # User business logic
│       │       ├── EmailService.java           # Email sending (OTP)
│       │       ├── FileStorageService.java     # File upload/download
│       │       └── NotificationService.java    # Notification logic
│       └── resources/
│           ├── application.properties          # App configuration
│           ├── data.sql                        # Seed / migration data
│           ├── static/css/style.css            # Global stylesheet
│           └── templates/
│               ├── login.html
│               ├── register.html
│               ├── forgot-password.html
│               ├── dashboard.html
│               ├── add-contract.html
│               ├── edit-contract.html
│               ├── view-contract.html
│               ├── reports.html
│               ├── notifications.html
│               ├── settings.html
│               ├── users.html
│               └── fragments/                  # Reusable Thymeleaf fragments
└── uploads/                                    # Uploaded contract files
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 17** or later — [Download](https://adoptium.net/)
- **Apache Maven 3.8+** — [Download](https://maven.apache.org/download.cgi)
- **MySQL 8.0+** — [Download](https://dev.mysql.com/downloads/)

### 1. Clone the Repository

```bash
git clone https://github.com/omkarpatil1405/contract-management-system.git
cd contract-management-system
```

### 2. Configure the Database

The application automatically creates the database if it doesn't exist. Ensure MySQL is running, then update `src/main/resources/application.properties` with your credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/cms_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_password
```

### 3. Configure Email (SMTP)

To enable the forgot-password OTP flow, configure your SMTP settings in `application.properties`:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

> [!TIP]
> For Gmail, generate an [App Password](https://support.google.com/accounts/answer/185833) instead of using your account password.

### 4. Build & Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on **http://localhost:8080**.

### 5. Access the Application

| URL                        | Description              |
|----------------------------|--------------------------|
| `http://localhost:8080`    | Redirects to Login       |
| `http://localhost:8080/login` | Login page            |
| `http://localhost:8080/register` | Registration page  |
| `http://localhost:8080/dashboard` | Main dashboard     |

---

## 📋 Data Model

### Contract

| Field          | Type         | Description                                |
|----------------|--------------|--------------------------------------------|
| `id`           | Long         | Auto-generated primary key                 |
| `title`        | String       | Contract title (required)                  |
| `description`  | String       | Detailed description (TEXT)                |
| `startDate`    | LocalDate    | Contract start date (required)             |
| `endDate`      | LocalDate    | Contract end date (required)               |
| `status`       | Enum         | `DRAFT`, `SIGNED`, `RUNNING`, `EXPIRED`    |
| `party`        | Enum         | `INTERNAL`, `EXTERNAL`, `GOVERNMENT`, `VENDOR`, `CLIENT` |
| `contractType` | String       | Contract type (default or custom)          |
| `fileName`     | String       | Attached document filename                 |
| `user`         | User (FK)    | Owner of the contract                      |

### User

| Field       | Type          | Description                             |
|-------------|---------------|-----------------------------------------|
| `id`        | Long          | Auto-generated primary key              |
| `fullName`  | String        | User's full name (required)             |
| `username`  | String        | Unique username (3–50 chars, required)  |
| `email`     | String        | Unique email address (required)         |
| `password`  | String        | BCrypt-hashed password (min 6 chars)    |
| `role`      | Enum          | `ADMIN` or `USER`                       |
| `otp`       | String        | One-time password for reset             |
| `otpExpiry` | LocalDateTime | OTP expiration timestamp                |
| `createdAt` | LocalDateTime | Account creation timestamp              |

### Notification

| Field       | Type          | Description                              |
|-------------|---------------|------------------------------------------|
| `id`        | Long          | Auto-generated primary key               |
| `title`     | String        | Notification title                       |
| `message`   | String        | Notification body (TEXT)                  |
| `type`      | Enum          | `INFO`, `WARNING`, `SUCCESS`, `DANGER`   |
| `read`      | boolean       | Read/unread status                       |
| `user`      | User (FK)     | Recipient of the notification            |
| `createdAt` | LocalDateTime | Notification creation timestamp          |

---

## 🔧 Configuration Reference

Key properties in `application.properties`:

| Property                                    | Default          | Description                       |
|---------------------------------------------|------------------|-----------------------------------|
| `server.port`                               | `8080`           | Application port                  |
| `spring.jpa.hibernate.ddl-auto`             | `update`         | Auto schema migration             |
| `spring.servlet.multipart.max-file-size`    | `5MB`            | Max upload file size              |
| `spring.servlet.multipart.max-request-size` | `10MB`           | Max request size                  |
| `server.servlet.session.timeout`            | `24h`            | Session timeout duration          |

---

## 🤝 Contributing

1. **Fork** the repository
2. **Create** a feature branch — `git checkout -b feature/your-feature`
3. **Commit** your changes — `git commit -m "Add your feature"`
4. **Push** to the branch — `git push origin feature/your-feature`
5. **Open** a Pull Request

---

## 📝 License

This project is open-source and available under the [MIT License](LICENSE).

---

## 👤 Author

**Omkar Patil** — [@omkarpatil1405](https://github.com/omkarpatil1405)
