# 🚛 Transport Services Web App

A Spring Boot web application for managing user accounts and transport-related services.  
Includes JWT authentication, email verification, phone number verification (2FA), and user profile management.

---

## 🔐 Features

- ✅ User Registration with Email Verification
- ✅ JWT-based Login & Session Handling
- ✅ Profile Page with User Details
- ✅ Phone Number Verification via SMS (Brevo-ready)
- ✅ Conditional UI Based on Login State
- ✅ Logout Functionality
- ✅ Reset Password feature
- 🔜 Change phone number and Add phone number are under maintenance.
- ✅ Edit Username & Email

---

## 🛠️ Tech Stack

- **Backend**: Spring Boot, Spring Security, JWT
- **Frontend**: Thymeleaf (no JS)
- **Database**: MySQL
- **Email**: JavaMailSender (SMTP)
- **SMS**: Brevo SMS API (planned)
- **Session**: HttpSession for login state
