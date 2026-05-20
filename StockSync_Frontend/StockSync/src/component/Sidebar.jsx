import React from "react";
import { Link } from "react-router-dom";
import ApiService from "../service/ApiService";


const Sidebar = ({ isOpen, toggleSidebar }) => {
    
    
    const handleLogout = () => {
        ApiService.logout();
        
        if (toggleSidebar) toggleSidebar(); 
    };

    
    const isAuth = ApiService.isAuthenticated();
    const isAdmin = ApiService.isAdmin();
    const isManager = ApiService.isManager();

   
    const sidebarClass = `sidebar ${isOpen ? 'open' : ''}`;

    return (
        
        <div className={sidebarClass}>
            <h1 className="ims">StockSync</h1> 
            <ul className="nav-links">
                
                
                {isAuth && (
                    <li>
                        
                        <Link to="/dashboard" onClick={toggleSidebar}>
                            Dashboard
                        </Link>
                    </li>
                )}

                
                {isAuth && (
                    <li>
                        <Link to="/transaction" onClick={toggleSidebar}>
                            Transactions
                        </Link>
                    </li>
                )}

                
                {isAdmin && (
                    <li>
                        <Link to="/category" onClick={toggleSidebar}>
                            Category
                        </Link>
                    </li>
                )}

                
                {(isAdmin || isManager) && (
                    <li>
                        <Link to="/product" onClick={toggleSidebar}>
                            Product
                        </Link>
                    </li>
                )}

                
                {isAdmin && (
                    <li>
                        <Link to="/supplier" onClick={toggleSidebar}>
                            Supplier
                        </Link>
                    </li>
                )}

                
                {isAuth && (
                    <li>
                        <Link to="/purchase" onClick={toggleSidebar}>
                            Purchase
                        </Link>
                    </li>
                )}

                
                {isAuth && (
                    <li>
                        <Link to="/sell" onClick={toggleSidebar}>
                            Sell
                        </Link>
                    </li>
                )}

                
                {isAuth && (
                    <li>
                        <Link to="/profile" onClick={toggleSidebar}>
                            Profile
                        </Link>
                    </li>
                )}

                
                {isAuth && (
                    <li>
                        
                        <Link onClick={handleLogout} to="/login">
                            Logout
                        </Link>
                    </li>
                )}
            </ul>
        </div>
    );
};

export default Sidebar;