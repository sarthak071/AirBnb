# Hotel Booking Management System (Spring Boot Backend)

## Project Motivation

The motivation for this project stems from my desire to master **Spring Boot** features and implement an industry-standard, real-world backend application. By drawing inspiration from platforms like **Airbnb**, my aim was to comprehensively cover advanced backend concepts including secure REST APIs, dynamic pricing, transactional safety, and scalable design for a robust hotel booking engine.

Key drivers:
- Deep-dive into Spring Boot and apply it as in production-grade systems.
- Clone and adapt business logic/concepts found in large-scale platforms.
- Learn and implement advanced design patterns (Decorator, Strategy).
- Explore solutions for real-time pricing, inventory management, and payments.

---

## Functional Overview

### Architecture Roles & Systems

| Role           | Systems Accessed          | Key Functionalities                                                               |
|----------------|--------------------------|-----------------------------------------------------------------------------------|
| **HotelManager** | Hotel Management System  | Create/manage hotels, rooms, room types, and bookings.                            |
| **Guest**        | Search & Booking Systems | Search hotels, get room details, create/manage bookings, make payments.           |

---

### Core Functionalities

#### HotelManager
- **Create & manage hotels** (CRUD)
- **Create & manage room types**
- **Manage bookings** (view, update, cancel)

#### Guest
- **Search hotels** by city, date range, room count
- **Get hotel/room details**
- **Book rooms:** attach guests, make payments, receive booking statuses (Confirmed/Failed)
- **View/manage bookings**

#### Booking/Inventory/Payment Flow

1. **Search:** Guests find available hotels using filters.
2. **Availability:** System validates live inventory, holds rooms using transactional JPA locks.
3. **Booking:** Guests input guest info, select room types, initiate payment (Stripe).
4. **Payment:** Upon success, booking is confirmed; otherwise, marked as failed.
5. **Management:** Both guests and managers can manage and review their bookings.

---

## System & Entity Design

<img width="987" height="721" alt="Screenshot 2025-07-22 at 3 13 49 AM" src="https://github.com/user-attachments/assets/e277094f-2fb8-46a7-a567-56dfd00bf5ee" />


- **Entities:** HotelManager, Guest, User, Hotel, Room, Inventory, Booking, Payment, BookingGuest, ContactInfo
- **ERD Highlights:** 
  - All bookings are atomic and consistent, users and guests are distinct, room counts tied to inventories, and payments linked to bookings for complete traceability[1][2][4].
<img width="1073" height="618" alt="Screenshot 2025-07-22 at 3 16 08 AM" src="https://github.com/user-attachments/assets/4f2d32b3-64c7-40af-b119-e86c36cb43ec" />
<img width="1221" height="696" alt="Screenshot 2025-07-22 at 3 17 09 AM" src="https://github.com/user-attachments/assets/d9c8f727-3ce2-4e32-bd0a-a937a96d06e6" />

---

## Design Patterns & Strategies

### Dynamic Pricing Strategy

Rates adapt based on several real-world factors:
- **BasePricingStrategy:** Default rate.
- **OccupancyPricingStrategy:** >80% booked increases price automatically.
- **UrgencyPricingStrategy:** Last-minute bookings incur higher rates.
- **HolidaysPricingStrategy:** Room rates surge for special/tourist days.
- **DiscountPricingStrategy:** Active promotions yield reductions.
- **Implementation:** *Decorator pattern* enables stacking strategies on-the-fly, ensuring flexibility and future extensibility[3].

### Inventory Consistency

- **Transactional Locking:** All inventory updates utilize JPA’s pessimistic transactional locks to avoid overbooking during high concurrency.

---

## REST API Endpoints

### HotelManager
- `POST /api/v1/admin/hotels`, `GET/DELETE/PATCH` hotel endpoints
- Manage rooms: `POST /api/v1/admin/hotels/{hotelId}/rooms`
- Manage bookings: `GET /api/v1/admin/bookings`
- Update inventory: `PATCH /api/v1/admin/inventory/{hotelId}/{roomId}/{date}`

### Guest
- `GET /api/v1/hotels/search` (with filters)
- Booking: `POST /api/v1/bookings`, manage guests, make payments
- Booking management: `GET/PATCH /api/v1/bookings`

### System (Automation)
- `PATCH /api/v1/bookings/resetBookings` (cron job, periodic reset)

**Full endpoint and parameter details provided in project documentation and Swagger/OpenAPI UI**[3].

---

## Implementation Highlights

- **Spring Boot** as the core framework
- **Spring JPA/Hibernate** for ORM and transactional data integrity
- **Stripe** integration for payments (secure, scalable)
- **JWT** for authentication, session, and security
- **Decorator/Strategy pattern** for flexible pricing module
- **Comprehensive ER Model**: Supports multi-user roles, real-world relationships, and transaction traceability[4].

---

## Challenges & Solutions

- **Dynamic Pricing:** Implemented scalable, real-time pricing using the Decorator pattern (with multiple strategy layers) for flexibility and testability.
- **Security:** Used robust JWT with role-based authorization, Stripe for encrypted payments, and Spring Security best practices.
- **Data consistency:** JPA transaction locking and inventory management ensure no race conditions or overbookings.
- **Industry-grade modularity:** Separated logical layers (controller, service, repository) for maintainability and extensibility.

---

## Future Enhancements

- Add alternate payment methods (e.g., UPI, PayPal).
- Implement large-scale concurrency (handle millions of users concurrently).
- Extend search with more filters, recommendations, reviews, etc.
- Add analytics dashboards for admins and hosts.
- Containerize and deploy using Kubernetes or Docker.

---

## How to Run

1. **Clone repo and install dependencies**
2. **Configure application properties:** DB, Stripe keys, security secrets.
3. **Use `mvn spring-boot:run` **
4. **Access API docs at `/swagger-ui.html`**

---

## Contact

For queries, collaborations, or feedback:  
*Sarthak Soumy*  
*sarthaksoumy11@gmail.com*

---
