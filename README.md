# Auction House - Weekly Batch Auction System

A comprehensive auction platform built with Spring Boot that conducts weekly batch auctions with a structured workflow for item submission, admin review, and live bidding.

## 🎯 Project Overview

This system implements a unique weekly auction cycle where:
- **Monday-Wednesday**: Sellers submit items for auction
- **Thursday-Friday**: Admins review and approve/reject submissions
- **Saturday 10 AM - Sunday 8 PM**: Live auction with real-time bidding
- **Automatic cycle**: System automatically transitions between phases

## 🏗️ Architecture

### System Design
- **Architecture Pattern**: Modular Monolithic (designed for future microservices extraction)
- **Design Pattern**: Layered Architecture (Controller → Service → Repository → Entity)
- **Database**: PostgreSQL with JPA/Hibernate
- **Caching**: Redis (planned)
- **Real-time**: WebSocket for live bid updates (planned)

### Technology Stack

**Backend:**
- Java 17
- Spring Boot 3.x
- Spring Data JPA
- Spring Security
- PostgreSQL 15
- Redis 7
- WebSocket (STOMP)
- Maven

**Planned Integrations:**
- JWT Authentication
- Email Notifications
- File Upload (AWS S3 / Local Storage)
- Payment Gateway Integration

## 📋 Features

### Current Implementation (Phase 1-2)
- ✅ User Management (Registration, Authentication, Roles)
- ✅ Weekly Batch Creation & Management
- ✅ Item Submission (Sellers)
- ✅ Admin Review System (Approve/Reject/Request Changes)
- ✅ Basic Bidding System
- ✅ Auction Lifecycle Management

### Planned Features (Phase 3-5)
- ⏳ Concurrent Bidding with Optimistic Locking
- ⏳ Real-time Bid Updates (WebSocket)
- ⏳ Automated Auction Scheduler
- ⏳ Email/Push Notifications
- ⏳ Search & Filters
- ⏳ Watchlist
- ⏳ Payment Processing
- ⏳ Transaction History
- ⏳ Admin Dashboard & Analytics

## 🗂️ Project Structure

```
auction-system/
├── src/main/java/com/auction/
│   ├── AuctionApplication.java
│   │
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── WebSocketConfig.java (planned)
│   │   └── RedisConfig.java (planned)
│   │
│   ├── common/
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── ResourceNotFoundException.java
│   │   │   └── BusinessException.java
│   │   ├── dto/
│   │   └── enums/
│   │       ├── BatchStatus.java
│   │       ├── ItemStatus.java
│   │       ├── BidStatus.java
│   │       ├── UserRole.java
│   │       └── TransactionStatus.java
│   │
│   ├── user/
│   │   ├── controller/
│   │   ├── service/
│   │   │   └── UserService.java
│   │   ├── repository/
│   │   │   └── UserRepository.java
│   │   ├── model/
│   │   │   └── User.java
│   │   └── dto/
│   │
│   ├── batch/
│   │   ├── controller/
│   │   ├── service/
│   │   │   └── AuctionBatchService.java
│   │   ├── repository/
│   │   │   └── AuctionBatchRepository.java
│   │   ├── model/
│   │   │   └── AuctionBatch.java
│   │   └── dto/
│   │
│   ├── item/
│   │   ├── controller/
│   │   ├── service/
│   │   │   └── ItemSubmissionService.java
│   │   ├── repository/
│   │   │   └── AuctionItemRepository.java
│   │   ├── model/
│   │   │   └── AuctionItem.java
│   │   └── dto/
│   │
│   ├── admin/
│   │   ├── controller/
│   │   ├── service/
│   │   │   └── AdminReviewService.java
│   │   └── dto/
│   │
│   ├── bid/
│   │   ├── controller/
│   │   ├── service/
│   │   │   └── BidService.java
│   │   ├── repository/
│   │   │   └── BidRepository.java
│   │   ├── model/
│   │   │   └── Bid.java
│   │   └── dto/
│   │
│   ├── notification/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── model/
│   │   └── event/
│   │
│   ├── payment/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── model/
│   │   │   └── Transaction.java
│   │   └── dto/
│   │
│   └── scheduler/
│       └── service/
│           └── AuctionLifecycleScheduler.java (planned)
│
├── src/main/resources/
│   ├── application.yml
│   └── application-dev.yml
│
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── README.md
```

## 🗄️ Database Schema

### Core Tables

**users**
- Primary entity for all system users (Buyers, Sellers, Admins)
- Fields: id, email, password, full_name, role, balance, is_verified, is_active

**auction_batches**
- Represents weekly auction cycles
- Fields: id, batch_code, week_number, year, status, submission dates, review dates, auction dates
- Tracks: total items submitted/approved/rejected/sold, total revenue

**auction_items**
- Items submitted for auction
- Fields: id, batch_id, seller_id, title, description, category, prices, status
- Status flow: SUBMITTED → UNDER_REVIEW → APPROVED/REJECTED → LIVE → SOLD/UNSOLD

**bids**
- Bid records for items
- Fields: id, item_id, bidder_id, amount, status, bid_time
- Includes optimistic locking for concurrency control

**transactions**
- Payment and settlement records
- Fields: id, auction_item_id, buyer_id, seller_id, amount, status, payment details

**notifications**
- User notifications for various events
- Fields: id, user_id, type, title, message, is_read

**watchlist**
- Users watching specific items
- Composite key: user_id + item_id

### Entity Relationships
```
User (1) ──→ (N) AuctionItem [as seller]
User (1) ──→ (N) Bid [as bidder]
User (1) ──→ (N) AuctionItem [as winner]
AuctionBatch (1) ──→ (N) AuctionItem
AuctionItem (1) ──→ (N) Bid
AuctionItem (1) ──→ (1) Transaction
User (N) ──→ (N) AuctionItem [via Watchlist]
```

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.8+
- PostgreSQL 15+
- Redis 7+ (optional for now)
- Git

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/auction-system.git
cd auction-system
```

2. **Configure Database**

Create PostgreSQL database:
```sql
CREATE DATABASE auction_db;
CREATE USER auction_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE auction_db TO auction_user;
```

3. **Update Configuration**

Edit `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/auction_db
    username: auction_user
    password: your_password
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

4. **Build the Project**
```bash
mvn clean install
```

5. **Run the Application**
```bash
mvn spring-boot:run
```

Application will start on `http://localhost:8080`

### Using Docker (Alternative)

1. **Start services with Docker Compose**
```bash
docker-compose up -d
```

This will start:
- PostgreSQL on port 5432
- Redis on port 6379
- Application on port 8080

2. **Stop services**
```bash
docker-compose down
```

## 📝 API Documentation

### Authentication (Planned)
```
POST   /api/auth/register    - Register new user
POST   /api/auth/login       - User login
POST   /api/auth/logout      - User logout
```

### User Management (Planned)
```
GET    /api/users/me         - Get current user profile
PUT    /api/users/me         - Update profile
POST   /api/users/me/balance - Add balance
```

### Auction Batches
```
GET    /api/batches/current  - Get current week's batch
GET    /api/batches/{id}     - Get batch details
GET    /api/batches          - List all batches (Admin)
```

### Item Management (Planned)
```
POST   /api/items/submit              - Submit item (Seller)
GET    /api/items/my-submissions      - View my submissions (Seller)
PUT    /api/items/{id}                - Update item (Seller)
DELETE /api/items/{id}                - Withdraw submission (Seller)
```

### Admin Review (Planned)
```
GET    /api/admin/items/pending       - Items pending review
POST   /api/admin/items/{id}/approve  - Approve item
POST   /api/admin/items/{id}/reject   - Reject item
POST   /api/admin/items/{id}/request-changes - Request changes
```

### Auction & Bidding (Planned)
```
GET    /api/auction/items             - Browse live items
GET    /api/auction/items/{id}        - Item details
POST   /api/items/{id}/bids           - Place bid
GET    /api/items/{id}/bids           - Bid history
```

## 🔄 Weekly Auction Workflow

### Phase 1: Submission (Monday 00:00 - Wednesday 23:59)
1. Sellers log in and submit items
2. Items enter `SUBMITTED` status
3. Batch tracks total submissions

### Phase 2: Review (Thursday 00:00 - Friday 23:59)
1. Admins review submitted items
2. Actions: Approve, Reject, or Request Changes
3. Approved items move to `APPROVED` status
4. Rejected items move to `REJECTED` status

### Phase 3: Auction (Saturday 10:00 AM - Sunday 8:00 PM)
1. System automatically starts auction
2. Approved items become `LIVE`
3. Buyers place bids in real-time
4. System tracks highest bid per item

### Phase 4: Settlement (Sunday 8:00 PM onwards)
1. System automatically ends auction
2. Determines winners (highest bid ≥ reserve price)
3. Creates transactions
4. Sends notifications
5. Items marked as `SOLD` or `UNSOLD`

### Phase 5: Cycle Reset (Monday 00:00)
1. New batch created for next week
2. Previous batch archived
3. Cycle repeats

## 🧪 Testing

### Run Tests
```bash
mvn test
```

### Test Coverage
```bash
mvn jacoco:report
```

### Manual Testing with Postman
1. Import Postman collection (planned)
2. Set environment variables
3. Test endpoints sequentially

## 🛣️ Development Roadmap

### ✅ Phase 1: Foundation (Week 1)
- [x] Project setup
- [x] Database schema design
- [x] Entity models
- [x] Repository layer
- [x] Basic service layer

### ✅ Phase 2: Core Business Logic (Week 2)
- [x] User management service
- [x] Batch management service
- [x] Item submission service
- [x] Admin review service
- [x] Basic bidding service

### 🔄 Phase 3: API & Automation (Week 3)
- [ ] REST controllers
- [ ] JWT authentication
- [ ] Automated scheduler for batch transitions
- [ ] Concurrent bidding with locking
- [ ] WebSocket for real-time updates

### ⏳ Phase 4: Enhanced Features (Week 4)
- [ ] Email notifications
- [ ] File upload for images
- [ ] Search and filters
- [ ] Watchlist functionality
- [ ] Admin dashboard

### ⏳ Phase 5: Production Ready (Week 5)
- [ ] Payment integration (Stripe/PayPal)
- [ ] Transaction management
- [ ] Comprehensive testing
- [ ] API documentation (Swagger)
- [ ] Deployment configuration
- [ ] Monitoring & logging

### 🚀 Future Enhancements
- [ ] Mobile app (React Native)
- [ ] Advanced analytics
- [ ] ML-based price recommendations
- [ ] Multi-language support
- [ ] Microservices architecture migration

## 🏛️ Design Decisions

### Why Monolithic First?
- Faster initial development
- Easier debugging and testing
- Single deployment unit
- Lower operational complexity
- Clear module boundaries for future extraction

### When to Extract to Microservices?
- **Bidding Service**: When concurrent load requires independent scaling
- **Notification Service**: Already loosely coupled, easy to extract
- **Payment Service**: When PCI compliance requires isolation
- Keep related functionality together, avoid over-fragmentation

### Optimistic Locking for Bids
- Uses `@Version` annotation
- Prevents race conditions in concurrent bidding
- Throws exception on conflict, client retries
- Better performance than pessimistic locking for read-heavy workloads

### Event-Driven Notifications
- Spring's `ApplicationEventPublisher` for internal events
- Decouples notification logic from business logic
- Easy to extend with external event bus (Kafka/RabbitMQ) later

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Style
- Follow Java naming conventions
- Use meaningful variable/method names
- Add JavaDoc for public methods
- Write unit tests for new features
- Keep services focused and single-responsibility

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👤 Author

**Your Name**
- GitHub: [@porfskylord](https://github.com/porfskylord)
- LinkedIn: [Azad_](https://www.linkedin.com/in/azad-2257721b4/)
- Email: theazad.jdev@gmail.com

## 🙏 Acknowledgments

- Spring Boot Documentation
- Baeldung Tutorials
- PostgreSQL Community
- Stack Overflow Community

## 📞 Support

For support, email theazad.jdev@gmail.com or open an issue on GitHub.

## 📊 Project Status

**Current Status**: 🟡 In Development (Phase 2 Complete)

**Latest Updates**:
- ✅ Database schema implemented
- ✅ Repository layer complete
- ✅ Service layer complete
- 🔄 Working on REST controllers

---

⭐ **Star this repository if you find it helpful!**

---

## 📸 Screenshots (Coming Soon)

- Seller Dashboard
- Admin Review Panel
- Live Auction Interface
- Bidding History
- Transaction History

## 🔗 Related Documentation

- [API Documentation](docs/API.md) (coming soon)
- [Database Schema](docs/DATABASE.md) (coming soon)
- [Deployment Guide](docs/DEPLOYMENT.md) (coming soon)
- [Architecture Decisions](docs/ADR.md) (coming soon)

---

**Last Updated**: January 2026
