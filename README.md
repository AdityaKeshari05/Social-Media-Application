# 🌐 Social Media & Blog Application - Full-Stack Application

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.7-brightgreen.svg)
![React](https://img.shields.io/badge/React-19.2.0-blue.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)

An advanced, highly-interactive full-stack social media and blogging platform. The project is designed with a scalable **Spring Boot** backend and a blazingly fast **React + TypeScript** frontend. It enables users to create profiles, publish posts, engage through comments and likes, build followers, and chat in real-time.

---

## ✨ Features

### 🛡️ User Authentication & Security
- Secure registration and login using **JWT (JSON Web Tokens)**.
- Password reset and secure password management.
- Spring Security integrated for robust role-based and endpoint-based authorization.

### 👤 Profile Management
- Comprehensive user profiles.
- Image uploads for avatars and cover photos backed securely by **Cloudinary**.
- Profile visibility configurations (Public/Private accounts).
- Complete Follow system: Follow/Unfollow, and handling of Follow Requests for private profiles.

### 📝 Posts & Content
- Create, read, update, and delete rich-text posts.
- Categorize content efficiently using a powerful Category system.
- Chronological newsfeed tailored to user's following list and visibility constraints.

### ❤️ Engagement & Interaction
- **Likes & Comments:** Double down on engagement by reacting to posts and comments.
- **Nested interactions:** Granular interaction management tied securely to follow-relations.

### 💬 Real-Time Features (WebSockets)
- **Direct Messaging:** Private chat between users handled seamlessly via STOMP/SockJS WebSockets over persistent connections.
- **Notifications:** Instant alerts for new followers, likes, comments, and direct messages.

---

## 🛠️ Tech Stack

### Backend Architecture
- **Java 21** & **Spring Boot 3.5.x**
- **Spring Security** & **JWT** for robust stateless authentication.
- **Spring Data JPA** coupled with **Hibernate** for database interactions.
- **MySQL** as the primary relational database.
- **Spring WebSockets** (STOMP) for bidirectional real-time data streaming.
- **Cloudinary** integration for optimized cloud image storage.
- **Spring Mail** for email lifecycle events.

### Frontend Architecture
- **React 19** & **TypeScript** initialized via **Vite**.
- **React Router DOM v7** for seamless Single Page Application (SPA) routing.
- **Axios** tailored for robust API consumption and interceptors.
- **StompJS & SockJS** for reliable WebSocket client implementations.
- **React Easy Crop** for interactive dynamic UI avatar modifications.
- Dedicated Context APIs (`AuthContext`, `MessagingContext`) for global state management.

---

## 🚀 Getting Started

### Prerequisites
- [Java 21](https://jdk.java.net/21/)
- [Node.js 20+](https://nodejs.org/) & npm
- [MySQL Server](https://www.mysql.com/)
- A [Cloudinary Account](https://cloudinary.com/) (required for cloud image uploads)

### 1. Backend Setup

1. Open the project inside your preferred Java IDE (IntelliJ IDEA, Eclipse, VS Code).
2. Configure your database and API credentials. To do this, establish the following environments in `application.properties` or `application.yml`:
   - MySQL Database URL, Username, and Password.
   - JWT Secret Key.
   - Cloudinary Configuration (`cloud_name`, `api_key`, `api_secret`).
   - Email SMTP Configuration (if utilizing mail components).
3. Build and launch the underlying architecture via the predefined Maven wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```
   *The backend will typically start on `http://localhost:8080`.*

### 2. Frontend Setup

1. Navigate to the frontend UI build directory:
   ```bash
   cd frontend
   ```
2. Install the application dependencies:
   ```bash
   npm install
   ```
3. Initialize the Vite development server:
   ```bash
   npm run dev
   ```
   *The frontend will typically be accessible via `http://localhost:5173`.*

---

## 📂 Project Structure Snapshot

- **`src/main/java/com/intermediate/Blog/Application`** (Backend Core)
  - `Controllers/` - REST API Endpoints encapsulating the presentation logic.
  - `Models/` - Core JPA Entities dictating table logic (User, Post, Comment, Message).
  - `Repositories/` - Spring Data Access Layer bindings.
  - `ServiceLayer/` - Modular, detached Business Logic execution.
  - `Security/` - JWT Filters and Auth configurations.
  - `Configurations/` - General Beans alongside WebSocket initialization points.

- **`frontend/src/`** (Frontend UI)
  - `components/` - Standardized reusable React components.
  - `pages/` - View-level compositions (e.g., Login, Profile, CreatePost).
  - `utils/` & `api.ts` - Axios instances configured for centralized backend calls.
  - `AuthContext.tsx` / `MessagingContext.tsx` - Cross-functional App Providers mapping out user and socket lifecycle events.

---

## 🤝 Contributing
Contributions, enhancements, and feature requests are always welcome! Constructive criticism and issue tracking are heavily encouraged.

## 📝 License
This project is distributed under the [MIT License](https://choosealicense.com/licenses/mit/).
