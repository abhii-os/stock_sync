// A single source for our fake security token, used by the auth middleware.
const FAKE_JWT = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc1NzY2NTcxOCwiZXhwIjoxNzU3NjY2NjE4LCJpc3MiOiJnZW5jX2NvaG9ydCIsImF1ZCI6WyJHZW5DIl19.TomHldZbSpcIc6bb6gRqpGPw6bm2RSwaHWmwzL_cXmo";

// A collection of standardized API responses, mostly for errors.
const apiResponses = {
  loginFailure: {
    success: false,
    message: 'Invalid email or password',
  },
  profileFailure: {
    success: false,
    message: 'User not found',
  },
  unauthorized: {
    error: 'Authentication required. No Authorization header.',
  },
  forbidden: {
    error: 'Forbidden. Invalid token.',
  },
};

module.exports = {
  FAKE_JWT,
  apiResponses,
};