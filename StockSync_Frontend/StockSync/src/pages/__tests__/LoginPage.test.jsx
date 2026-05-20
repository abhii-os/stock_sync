import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import LoginPage from '../LoginPage';
import ApiService from '../../service/ApiService';
import '@testing-library/jest-dom';

jest.mock('../../service/ApiService');

const renderWithRouter = (ui) => {
  return render(<BrowserRouter>{ui}</BrowserRouter>);
};

describe('LoginPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
  });

  it('renders login form', () => {
    renderWithRouter(<LoginPage />);
    expect(screen.getByPlaceholderText(/email/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /login/i })).toBeInTheDocument();
  });

  it('shows error on failed login', async () => {
    ApiService.loginUser.mockRejectedValue({ response: { data: { message: 'Invalid email or password.' } } });
    renderWithRouter(<LoginPage />);
    fireEvent.change(screen.getByPlaceholderText(/email/i), { target: { value: 'wrong@example.com' } });
    fireEvent.change(screen.getByPlaceholderText(/password/i), { target: { value: 'wrongpass' } });
    fireEvent.click(screen.getByRole('button', { name: /login/i }));
    
    await waitFor(() => expect(ApiService.loginUser).toHaveBeenCalled());
    
    await new Promise((r) => setTimeout(r, 0));
    
    console.log(document.body.innerHTML);
    
    const error = screen.queryByText(/invalid email or password/i);
    expect(error).not.toBeNull();
  });

  it('redirects on successful login', async () => {
    ApiService.loginUser.mockResolvedValue({ jwt: 'fake-jwt', id: 1, name: 'Test User', email: 'test@example.com' });
    renderWithRouter(<LoginPage />);
    fireEvent.change(screen.getByPlaceholderText(/email/i), { target: { value: 'test@example.com' } });
    fireEvent.change(screen.getByPlaceholderText(/password/i), { target: { value: 'password' } });
    fireEvent.click(screen.getByRole('button', { name: /login/i }));
    await waitFor(() => {
      expect(localStorage.getItem('token')).toBe('fake-jwt');
      expect(localStorage.getItem('user')).toContain('Test User');
    });
  });
});
