# MealFlow API Guide

This comprehensive guide documents all endpoints available in the MealFlow ecosystem, including both the Node.js Main Service and Python Recommendation Service.

## Table of Contents

- [Base URLs](#base-urls)
- [Authentication](#authentication)
- [Node.js Main Service](#nodejs-main-service)
  - [Authentication](#authentication-endpoints)
  - [User Management](#user-management-endpoints)
  - [Community](#community-endpoints)
  - [Meals (Node.js)](#meals-nodejs-endpoints)
  - [Upload](#upload-endpoints)
  - [System](#system-endpoints)
- [Python Recommendation Service](#python-recommendation-service)
  - [Recommendations](#recommendations-endpoints)
  - [Recipe Management](#recipe-management-endpoints)
  - [System Management](#system-management-endpoints)
- [Parameter Reference](#parameter-reference)

## Base URLs

- **Node.js Main Service**: `http://localhost:3000`
- **Python Recommendation Service**: `http://localhost:9999`

## Authentication

Most endpoints in the Node.js Main Service require authentication. Authentication is handled via JWT tokens:

- **Access Token**: Short-lived token (6 hours) used for API requests
- **Refresh Token**: Long-lived token (1 year) used to obtain new access tokens

Include the access token in the Authorization header:

```
Authorization: Bearer <access_token>
```

## Node.js Main Service

### Authentication Endpoints

#### Register User
- **Endpoint**: `POST /api/v1/users/register`
- **Description**: Register a new user
- **Body Parameters**:
  - `username`: String (3-30 characters, alphanumeric with underscores, must start with a letter)
  - `email`: String (valid email format)
  - `password`: String (8-64 characters, must include uppercase, lowercase, number, and special character)
- **Response**: Success message with instructions to verify email

#### Verify Email
- **Endpoint**: `POST /api/v1/users/verify-email`
- **Description**: Verify user email with OTP code
- **Body Parameters**:
  - `email`: String (email address)
  - `otp`: String (6-digit verification code)
- **Response**: Access token, refresh token, and user information

#### Resend Verification
- **Endpoint**: `POST /api/v1/users/resend-verification`
- **Description**: Resend verification OTP to user's email
- **Body Parameters**:
  - `email`: String (email address)
- **Response**: Success message

#### Login
- **Endpoint**: `POST /api/v1/users/login`
- **Description**: Login with email and password
- **Body Parameters**:
  - `email`: String (email address)
  - `password`: String (password)
- **Response**: Access token, refresh token, and user information

#### Logout
- **Endpoint**: `POST /api/v1/users/logout`
- **Description**: Logout and invalidate refresh token
- **Body Parameters**:
  - `refreshToken`: String (refresh token)
- **Response**: Success message

#### Quick Login
- **Endpoint**: `POST /api/v1/users/quick-login`
- **Description**: Quick login using refresh token
- **Body Parameters**:
  - `refreshToken`: String (refresh token)
- **Response**: Access token, refresh token, and user information

#### Refresh Token
- **Endpoint**: `GET /api/v1/users/refresh-token`
- **Description**: Get new access token using refresh token
- **Body Parameters**:
  - `refreshToken`: String (refresh token)
- **Response**: New access token

#### Forgot Password
- **Endpoint**: `POST /api/v1/users/forgot-password`
- **Description**: Send password reset link to email
- **Body Parameters**:
  - `email`: String (email address)
- **Response**: Success message and reset token

#### Reset Password
- **Endpoint**: `POST /api/v1/users/reset-password`
- **Description**: Reset password with token
- **Body Parameters**:
  - `token`: String (reset token)
  - `password`: String (new password, must meet password criteria)
- **Response**: Success message

### User Management Endpoints

#### Get User Profile
- **Endpoint**: `GET /api/v1/users/me`
- **Description**: Get current user profile
- **Authentication**: Required
- **Response**: User profile information

#### Update User Profile
- **Endpoint**: `PATCH /api/v1/users/me`
- **Description**: Update current user profile
- **Authentication**: Required
- **Body Parameters**: User profile fields to update
- **Response**: Updated user profile

#### Delete User Account
- **Endpoint**: `DELETE /api/v1/users/me`
- **Description**: Delete current user account
- **Authentication**: Required
- **Response**: Success message

### Community Endpoints

#### Create Community
- **Endpoint**: `POST /api/v1/community`
- **Description**: Create a new community
- **Authentication**: Required
- **Body Parameters**:
  - `name`: String (3-50 characters)
  - `description`: String (optional)
  - `privacy`: String (enum: "PUBLIC", "PRIVATE", "RESTRICTED")
  - `recipeCreationPermission`: String (enum: "ANY_MEMBER", "ADMIN_ONLY")
  - `categories`: Array of strings or JSON string (optional)
  - `image`: File (image file, optional)
- **Response**: Created community

#### Get All Communities
- **Endpoint**: `GET /api/v1/community`
- **Description**: Get all communities
- **Authentication**: Required
- **Response**: List of communities

#### Get Community by ID
- **Endpoint**: `GET /api/v1/community/:id`
- **Description**: Get community details by ID
- **Authentication**: Required
- **Path Parameters**:
  - `id`: String (community ID)
- **Response**: Community details

#### Join Community
- **Endpoint**: `POST /api/v1/community/:id/join`
- **Description**: Join a community
- **Authentication**: Required
- **Path Parameters**:
  - `id`: String (community ID)
- **Response**: Success message

#### Leave Community
- **Endpoint**: `DELETE /api/v1/community/:id/leave`
- **Description**: Leave a community
- **Authentication**: Required
- **Path Parameters**:
  - `id`: String (community ID)
- **Response**: Success message

#### Get Community Members
- **Endpoint**: `GET /api/v1/community/:id/members`
- **Description**: Get all members of a community
- **Authentication**: Required
- **Path Parameters**:
  - `id`: String (community ID)
- **Response**: List of community members


### Upload Endpoints

#### Upload Image
- **Endpoint**: `POST /api/v1/upload`
- **Description**: Upload an image to the server
- **Authentication**: Required
- **Body Parameters**:
  - `image`: File (image file)
- **Response**: Upload details

## Parameter Reference

### URL Configuration
- `base_url`: Default values:
  - Node.js: `http://localhost:3000`
  - Python: `http://localhost:9999/api/v1`

### User and Interaction Variables
- `user_id`: String identifier (use "default_user" for non-authenticated)
- `interaction_type`: Enum values:
  - `view`
  - `like`
  - `save`
  - `cook`
  - `rating`

### Recipe Filtering Variables

#### Geographical Filters
- `cuisine` / `region`: String values:
  - `Middle Eastern`
  - `Egyptian`
  - `Lebanese`
  - `Turkish`
  - `Saudi Arabian`
  - `Iraqi`
  - `Palestinian`

- `sub_region`: Same values as cuisine/region plus:
  - `Rest Middle Eastern`

- `continent`: String values:
  - `Africa`
  - Other continents (not in sample data)

#### Dietary Filters
- `dietaryTags`: String values:
  - `vegan` (494 recipes in database)
  - `pescetarian` (539 recipes)
  - `lacto_vegetarian` (299 recipes)

### Pagination and Limits
- `limit`: 
  - Default: 10
  - Maximum: 100
  - Any positive integer

- `page`: 
  - Default: 1
  - Any positive integer

### Numeric Recipe Variables

#### Time
- `cookTime`: Range: 5-120 minutes (most recipes: < 15 minutes)
- `prepTime`: Range: 10-30 minutes (average: ~15 minutes)

#### Nutrition
- `calories`: Range: 45-934, Categories:
  - Low (< 300): 2,021 recipes
  - Medium (300-600): 1,227 recipes
  - High (601-900): 370 recipes
  - Very High (> 900): 286 recipes

- `rating`: Decimal between 1-5 (e.g., 4.5)

### Recipe Identification
- `recipe_id`: Numeric IDs (sample range: 2610-2619)
  - Total recipes: 3,905

### Community Variables
- `privacy`: Enum values:
  - `PUBLIC`
  - `PRIVATE`
  - `RESTRICTED`

- `recipeCreationPermission`: Enum values:
  - `ANY_MEMBER`
  - `ADMIN_ONLY`

### Categories
Common categories include:
- `Italian Cuisine`
- `French Cuisine`
- `Mexican Cuisine`
- `Middle Eastern Cuisine`
- `Asian Cuisine`
- `Vegan & Plant-Based`
- `Vegetarian`
- `Keto & Low-Carb`
- `Breakfast & Brunch`
- `Dinner & Main Courses`
- `Home Cooking`