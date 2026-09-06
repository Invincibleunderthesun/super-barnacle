# E-Commerce API

A production-ready e-commerce REST API built with Spring Boot, featuring JWT authentication, role-based access control, and comprehensive product/order management.

## 🚀 Features

- **Authentication & Authorization**
  - JWT-based authentication
  - Role-based access control (ADMIN/USER)
  - Token refresh capability

- **Product Management**
  - CRUD operations
  - Search and filtering
  - Category management
  - Stock tracking with low-stock alerts

- **Shopping Cart**
  - Add/remove products
  - Cart history tracking
  - Calculate totals

- **Order Management**
  - Checkout with stock validation
  - Order status tracking (PENDING → SHIPPED → DELIVERED)
  - Order history
  - Cancellation with stock restoration
  - Order statistics (Admin)

- **API Features**
  - Swagger/OpenAPI documentation
  - Pagination and sorting
  - Global exception handling
  - Caching for performance

## 🛠 Tech Stack

- **Backend**: Spring Boot 3.2.5
- **Security**: Spring Security + JWT
- **Database**: PostgreSQL (H2 for development)
- **Documentation**: SpringDoc OpenAPI (Swagger)
- **Build**: Maven
- **Deployment**: Docker, Railway

## 📋 Prerequisites

- Java 17+
- Maven 3.8+
- Docker (optional, for containerized deployment)

## 🏃 Running Locally

### Option 1: Maven (Development Mode)

```bash
cd uday
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`

### Option 2: Docker Compose

```bash
docker-compose up -d
```

This starts both the API and PostgreSQL database.

## 📚 API Documentation

Once running, access the Swagger UI:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs (JSON)**: http://localhost:8080/api-docs

## 🔐 Default Credentials

| Role  | Email              | Password   |
|-------|-------------------|------------|
| Admin | admin@example.com | adminpass  |

## 📡 API Endpoints

### Authentication
| Method | Endpoint                    | Description          | Access  |
|--------|----------------------------|----------------------|---------|
| POST   | `/api/v1/auth/register`    | Register new user    | Public  |
| POST   | `/api/v1/auth/login`       | Login & get token    | Public  |
| POST   | `/api/v1/auth/refresh`     | Refresh JWT token    | Auth    |

### Products
| Method | Endpoint                        | Description           | Access  |
|--------|--------------------------------|-----------------------|---------|
| GET    | `/api/v1/products`             | List products (paginated) | Public  |
| GET    | `/api/v1/products/{id}`        | Get product by ID     | Public  |
| GET    | `/api/v1/products/search`      | Search products       | Public  |
| POST   | `/api/v1/products`             | Create product        | Admin   |
| PUT    | `/api/v1/products/{id}`        | Update product        | Admin   |
| DELETE | `/api/v1/products/{id}`        | Delete product        | Admin   |

### Cart
| Method | Endpoint                              | Description           | Access  |
|--------|--------------------------------------|-----------------------|---------|
| GET    | `/api/v1/cart/user/{userId}`         | Get user's cart       | User    |
| POST   | `/api/v1/cart/user/{userId}/add/{productId}` | Add to cart   | User    |
| DELETE | `/api/v1/cart/user/{userId}/remove/{productId}` | Remove from cart | User |
| DELETE | `/api/v1/cart/user/{userId}/clear`   | Clear cart            | User    |

### Orders
| Method | Endpoint                              | Description           | Access  |
|--------|--------------------------------------|-----------------------|---------|
| POST   | `/api/v1/orders/checkout/user/{userId}` | Checkout cart      | User    |
| GET    | `/api/v1/orders/user/{userId}`       | Get user's orders     | User    |
| GET    | `/api/v1/orders/{id}`                | Get order by ID       | User    |
| PUT    | `/api/v1/orders/{id}/status/{status}` | Update status        | Admin   |
| DELETE | `/api/v1/orders/{id}/cancel/user/{userId}` | Cancel order    | User    |
| GET    | `/api/v1/orders/stats`               | Order statistics      | Admin   |

## 🚀 Deployment to Railway

### Quick Deploy

1. Push your code to GitHub
2. Go to [Railway](https://railway.app)
3. Click "New Project" → "Deploy from GitHub repo"
4. Select your repository
5. Railway will auto-detect the Dockerfile

### Add PostgreSQL Database

1. In your Railway project, click "New" → "Database" → "PostgreSQL"
2. Railway automatically sets `DATABASE_URL`

### Configure Environment Variables

Add these in Railway dashboard → Variables:

```
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET=your_secure_256_bit_secret
JWT_EXPIRATION=3600000
```

### Your API is Live! 🎉

Railway provides a URL like: `https://your-app.railway.app`

## 🔧 Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DATABASE_URL` | PostgreSQL connection URL | - |
| `DB_USERNAME` | Database username | postgres |
| `DB_PASSWORD` | Database password | - |
| `JWT_SECRET` | JWT signing secret (min 256 bits) | - |
| `JWT_EXPIRATION` | Token expiry in ms | 3600000 |
| `PORT` | Server port | 8080 |
| `SPRING_PROFILES_ACTIVE` | Active profile (dev/prod) | dev |

## 📁 Project Structure

```
src/main/java/com/harsh/uday/
├── config/           # Configuration classes
├── controller/       # REST controllers
├── dto/              # Data Transfer Objects
├── exception/        # Exception handling
├── model/            # JPA entities
├── repository/       # JPA repositories
├── security/         # JWT & Security
└── service/          # Business logic
```

## 🧪 Testing

```bash
# Run tests
./mvnw test

# Build without tests
./mvnw clean package -DskipTests
```

## 📈 Future Improvements

- [ ] Add Redis for distributed caching
- [ ] Implement WebSocket for real-time updates
- [ ] Add payment gateway integration
- [ ] Email notifications
- [ ] Rate limiting
- [ ] Kubernetes deployment

## 📄 License

MIT License - feel free to use this project for learning or commercial purposes.
