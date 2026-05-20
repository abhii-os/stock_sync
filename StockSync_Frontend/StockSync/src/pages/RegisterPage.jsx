import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import ApiService from "../service/ApiService";

const RegisterPage = () => {
  
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [phone, setPhone] = useState(""); 
  const [message, setMessage] = useState("");
  

  const navigate = useNavigate();

  const handleRegister = async (e) => {
    e.preventDefault();
    
    try {
      
      const registerData = {
        username,
        email,
        password,
        firstName,
        lastName,
        phone,
        roleType: "ROLE_MANAGER" 
      };
      console.log("Sending this data to backend:", registerData);
      await ApiService.registerUser(registerData);
      setMessage("Registration Successfull");
      navigate("/login");

    } catch (error) {
      showMessage(
        error.message
      );
      console.log(error);
    }
  };

  const showMessage = (msg) => {
    setMessage(msg);
    setTimeout(() => {
      setMessage("");
    }, 4000);
  };
 

  return (
    <div className="auth-container">
      <h2>Register</h2>

      {message && <p className="message">{message}</p>}

      
      <form onSubmit={handleRegister}>
        <input
          type="text"
          placeholder="Username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          required
        />
        
        
        <input
          type="text"
          placeholder="First Name"
          value={firstName}
          onChange={(e) => setFirstName(e.target.value)}
          required
        />
        
        
        <input
          type="text"
          placeholder="Last Name"
          value={lastName}
          onChange={(e) => setLastName(e.target.value)}
          required
        />
        

        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        

        <input
          type="password" 
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        

        <input
          type="text"
          placeholder="Phone Number" 
          value={phone} 
          onChange={(e) => setPhone(e.target.value)}
          required
        />
        

        <button type="submit">Register</button>
      </form>
      <p>Already have an account? <a href="/login">Login</a></p>
    </div>
  );
};

export default RegisterPage;