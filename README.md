# WageWatch: Algorithmic Wage Transparency Platform

WageWatch is a full-stack SaaS platform designed to bring radical transparency to the gig economy. It empowers delivery drivers to audit their paychecks, expose algorithmic wage theft (distance shaving), and collaboratively map unfair pay zones in real-time.

---

## The Problem vs. The Solution

* **The Problem:** Delivery platforms use "black box" routing algorithms to calculate driver pay based on highly optimistic, straight-line estimates made before a trip begins. When drivers face real-world detours, they are not compensated for the actual distance driven.
* **The Solution:** WageWatch allows drivers to upload a screenshot of their earnings. The application uses AI (Google Gemini OCR) to extract the platform's claimed distance and pay, dynamically reverse-engineers the exact pay rate, and compares it against the driver's actual odometer reading. Verified discrepancies are then plotted on a collaborative city heatmap.

---

## Technology Stack

* **Frontend:** React.js (Vite), Axios, React-Leaflet, Vanilla CSS3 (Custom Dark Mode UI).
* **Backend:** Java 17, Spring Boot, Spring Data JPA, Hibernate.
* **Database:** PostgreSQL.
* **External APIs:** Google Gemini API (OCR), OpenRouteService (Spatial Validation).

---

## System Architecture (3-Tier)

1. **Presentation Layer:** React SPA interacting with users, mapping tools (Leaflet), and managing state.
2. **Business Logic Layer:** Spring Boot REST Controllers and Services executing spatial guardrails and dynamic wage math.
3. **Data Access Layer:** PostgreSQL relational database tracking Drivers to Trips via Hibernate ORM.

---

## Local Setup & Installation

### Prerequisites
* Node.js (v18 or higher)
* Java JDK 17 & Maven
* PostgreSQL

### 1. Database Setup
Create a new database in your PostgreSQL instance:
```sql
CREATE DATABASE wagewatch_db;

Update your src/main/resources/application.properties in the backend folder:

Properties
spring.datasource.url=jdbc:postgresql://localhost:5432/wagewatch_db
spring.datasource.username=YOUR_POSTGRES_USERNAME
spring.datasource.password=YOUR_POSTGRES_PASSWORD
spring.jpa.hibernate.ddl-auto=update
gemini.api.key=YOUR_GEMINI_API_KEY
ors.api.key=YOUR_OPENROUTESERVICE_KEY
How to Run the App (One-Click Script)
To avoid opening multiple terminals every time you want to code, use the batch script included in the root folder.

Ensure you are in the root WageWatch-Master folder.

Create a file named run.bat.

Paste the following code into run.bat:

DOS
@echo off
echo =======================================
echo Starting WageWatch Platform
echo =======================================

echo 1. Starting Spring Boot Backend...
start "WageWatch Backend"
cmd /k "cd analytics && mvn spring-boot:run"

echo 2. Starting React Frontend...
start "WageWatch Frontend"
cmd /k "cd wagewatch-frontend && npm run dev"

echo.
echo Applications are launching in separate windows.
echo Frontend: http://localhost:5173
echo Backend: http://localhost:8080
echo =======================================
pause
Double-click run.bat to launch both the frontend and backend simultaneously.

How to Push to GitHub
If you are cloning this repository or pushing your own updates, run these exact commands from the root WageWatch-Master folder terminal:

Bash
# 1. Initialize the repository (if not done already)
git init

# 2. Stage all files (respecting the master .gitignore)
git add .

# 3. Commit your changes
git commit -m "Update: WageWatch Fullstack Platform"

# 4. Set the main branch
git branch -M main

# 5. Connect to your GitHub repository (replace URL with your own)
git remote add origin [https://github.com/YOUR-USERNAME/wagewatch.git](https://github.com/YOUR-USERNAME/wagewatch.git)

# 6. Push the code to the cloud
git push -u origin main
