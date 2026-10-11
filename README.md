# Simply Sip

A mobile app for CSUN students to discover and filter drinks near campus based on dietary restrictions, allergies, calories, sugar content, and nutrition preferences.

## Tech Stack

- Frontend: React Native, Expo
- Backend: Java (Spring Boot), MySQL
- API: REST with JWT authentication

## Team

- Nicole Uribe — Team Lead / Scrum Master, Frontend Lead
- Angelo Rodriguez — Database Administrator, Backend Lead
- Cosette Espino — Graphic Designer, Backend
- Amin Zoghlami — Frontend, Backend
- Faith Hill — Frontend
- Danielle Pacheco — Backend


## Backend

The Spring Boot API lives in [`backend/`](backend/). Requires **Java 17+** and local **MySQL**.

### 1. Create the database

```sql
CREATE DATABASE simply_sip;
-- or use an existing DB and set spring.datasource.url in application-local.properties
```

### 2. Configure credentials

```bash
cp backend/src/main/resources/application-local.properties.example \
   backend/src/main/resources/application-local.properties
```

Edit `application-local.properties` with your MySQL username/password (gitignored).

### 3. Run the API

```bash
cd backend
./mvnw spring-boot:run
```

API: **http://localhost:8080**

Demo users (password `password123`):

- `alice@csun.edu`
- `bob@csun.edu`
- `cara@csun.edu`

### API endpoints

| Resource | Base path |
|----------|-----------|
| Auth | `POST /api/auth/register`, `POST /api/auth/login` |
| Users | `GET /api/users/me` (JWT), `GET/PUT /api/users/{id}` |
| Locations | `GET /api/locations`, CRUD |
| Drinks | `GET /api/drinks?category&locationId&q&maxCalories&maxSugar&tag`, CRUD |
| Favorites | `GET/POST /api/favorites`, `DELETE /api/favorites/{userId}/{drinkId}` (JWT) |

## Frontend

The Expo app is located in [`frontend/`](frontend/).

### Install dependencies

```bash
cd frontend
npm install
```

### Start the Expo app

```bash
npx expo start
```

You can also use:

```bash
npm start
```

### Run on a specific platform

```bash
npm run ios
npm run android
npm run web
```

### Test and validate the frontend

```bash
npm test -- --runInBand
npx expo-doctor
npm run lint
```

## Running the Full Project

Use two terminal windows.

### Terminal 1: Backend

```bash
cd backend
./mvnw spring-boot:run
```

The backend runs at:

```text
http://localhost:8080
```

### Terminal 2: Frontend

```bash
cd frontend
npx expo start
```
