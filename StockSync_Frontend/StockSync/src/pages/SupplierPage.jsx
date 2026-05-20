import React, { useState, useEffect } from "react";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";
import { useNavigate } from "react-router-dom";

const SupplierPage = () => {
  const [suppliers, setSuppliers] = useState([]);
  const [message, setMessage] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();

  
  const showMessage = (msg) => {
    setMessage(msg);
    setTimeout(() => {
      setMessage("");
    }, 4000);
  };

  
  const getSuppliers = async () => {
    setIsLoading(true);
    try {
      
      const response = await ApiService.getAllSuppliers();
      
      const responseData = response.data || response; 

      if (responseData.status === 200) {
        setSuppliers(responseData.suppliers || []); 
      } else {
        showMessage(responseData.message || "Failed to fetch suppliers.");
      }
    } catch (error) {
      
      showMessage(
        error.response?.data?.message || "Error Getting Suppliers: " + error.message
      );
      console.error("Fetch Error:", error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    getSuppliers();
  }, []);

  
  const handleDeleteSupplier = async (supplierId) => {
    if (window.confirm("Are you sure you want to delete this supplier?")) {
      try {
        
        const response = await ApiService.deleteSupplier(supplierId);
        const responseData = response.data || response; 

        if (responseData.status === 200 || response.status === 204) {
             
             setSuppliers((prevSuppliers) => 
               prevSuppliers.filter((supplier) => supplier.id !== supplierId)
             );
             showMessage("Supplier deleted successfully.");
        } else {
             showMessage(responseData.message || "Deletion failed.");
        }
       
      } catch (error) {
        showMessage(
          error.response?.data?.message || "Deletion failed: " + error.message
        );
        console.error("Delete Error:", error);
      }
    }
  };

  
  return (
    <Layout>
      
      {message && <div className="message success">{message}</div>}
      
      <div className="supplier-page">
        <div className="supplier-header">
          <h1>Suppliers</h1>
          
          <div className="add-sup"> 
            <button onClick={() => navigate("/add-supplier")}>
              Add Supplier
            </button>
          </div>
        </div>

        
        {isLoading && <p className="loading-message">Loading suppliers...</p>}
        
        
        {!isLoading && suppliers.length === 0 && (
            <p className="no-data-message">No suppliers found. Click 'Add Supplier' to create one.</p>
        )}

        
        {!isLoading && suppliers.length > 0 && (
          <ul className="supplier-list">
            {suppliers.map((supplier) => (
              <li className="supplier-item" key={supplier.id}>
                <div className="supplier-details">
                  
                  <strong className="supplier-name">{supplier.name}</strong> 
                  
                  {supplier.contactPerson && <span className="supplier-contact"> | Contact: {supplier.contactPerson}</span>}
                  {supplier.phone && <span className="supplier-phone"> | Phone: {supplier.phone}</span>}
                </div>

                <div className="supplier-actions">
                  
                  <button onClick={() => navigate(`/edit-supplier/${supplier.id}`)}>
                    Edit
                  </button>
                  
                  <button onClick={() => handleDeleteSupplier(supplier.id)}>
                    Delete
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </Layout>
  );
};

export default SupplierPage;