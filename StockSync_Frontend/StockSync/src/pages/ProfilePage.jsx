import React, { useState, useEffect } from "react";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService"; 

const ProfilePage = () => {
  
  const [user, setUser] = useState(ApiService.getUser()); 
  const [message, setMessage] = useState(null);

  useEffect(() => {
    
    if (!user) {
        
        showMessage("Could not find user data. Please log in again.");
    }
  }, [user]); 

  
  const showMessage = (msg) => {
    setMessage(msg);
    setTimeout(() => {
      setMessage("");
    }, 4000);
  };

  return (
    <Layout>
      {message && <div className="message">{message}</div>}
      <div className="profile-page">
        
        
        {user && (
          <div className="profile-card">
            
            
            <h1>Hello, {user.firstName || user.email || user.username} </h1>
            
            <div className="profile-info">
              
              
              <div className="profile-item">
                <label>Name</label>
                <span>{user.firstName} {user.lastName}</span>
              </div>
              
              
              <div className="profile-item">
                <label>Email</label>
                <span>{user.email}</span>
              </div>
              
              
              <div className="profile-item">
                <label>Phone Number</label>
                <span>{user.phone || "N/A"}</span>
              </div>
              
              
              <div className="profile-item">
                <label>Role</label>
                
                <span>{user.role}</span>
              </div>
            </div>
          </div>
        )}
        
        
      </div>
    </Layout>
  );
};
export default ProfilePage;