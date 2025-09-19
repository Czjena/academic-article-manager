Academic Article Manager

A Spring Boot-based backend API for managing scientific articles, reviewer assignments, and user roles. This project is integrated with Supabase for database and authentication access via RESTful API using WebClient.

📦 Features

Article submission and management

Reviewer account creation and role assignment

Assigning reviewers to articles

Role-based authorization (admin)

JWT security using oauth2ResourceServer.jwt

⚙️ Tech Stack

Java 21

Spring Boot

Spring WebFlux

Spring Security (JWT Resource Server)

Supabase (PostgreSQL + Auth)

WebClient for external HTTP communication

Lombok for boilerplate code reduction

🧪 Example Endpoints

PATCH /api/reviewers/{userId}/role

Assigns the "reviewer" role to an existing user by their Supabase UUID.

Example:

PATCH /api/reviewers/3a8f5c7c-1234-4d9a-bb09-e0bc24564bd4/role

Response: 204 No Content

POST /api/reviewers/assign/{articleId}/{reviewerId}

Assigns a reviewer to a specific article.

Headers:

Authorization: Bearer <admin-token>

Example:

POST /api/reviewers/assign/8f3e94aa-3122-4b89-aea3-abc123e7b3f4/dcbf3f65-e43d-4c88-9671-39b7359f6b9c

Response: 200 OK

🔐 Security

The app uses JWT tokens validated via oauth2ResourceServer.jwt mechanism. Admin routes like reviewer assignment are protected and require the admin role.

🧱 Architecture

ReviewerController – handles reviewer role assignment and article-reviewer linking

ReviewerService – interacts with Supabase Auth to add roles

AssignReviewerService – sends Supabase HTTP requests to connect articles with reviewers

VerifyRole – verifies if JWT contains necessary role (admin)

📁 Configuration

Add your Supabase service role key in your application-secret.properties.yml:

supabase.url
,supabase.api-key
,JWT_SECRET
,service.role-key
,articles.file_url
,auth.url
,auth.url-login

⚠️ Never commit application-secret.yml to version control.

🚧 Development

git clone https://github.com/Czjena/academic-article-manager.git
cd academic-article-manager
./mvnw spring-boot:run

Requires Java 21 and Supabase project set up with relevant tables and policies.

📜 License

This project is for academic and educational purposes.
