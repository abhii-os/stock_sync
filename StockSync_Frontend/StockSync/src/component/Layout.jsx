import React, { useState } from "react";
import Sidebar from "./Sidebar";


const Layout = ({children}) =>{
    
    const [isSidebarOpen, setIsSidebarOpen] = useState(false);

    
    const toggleSidebar = () => {
        setIsSidebarOpen(!isSidebarOpen);
    };

    return(
        <div className="layout">
            
            
            <button className="menu-toggle" onClick={toggleSidebar}>
                ☰
            </button>

            
            <Sidebar isOpen={isSidebarOpen} toggleSidebar={toggleSidebar} />

            
            <div className="main-content">
                {children}
            </div>
        </div>
    );
}

export default Layout;
