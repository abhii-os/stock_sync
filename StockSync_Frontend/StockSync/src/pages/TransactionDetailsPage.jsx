import React, { useState, useEffect } from "react";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";
import { useNavigate, useParams } from "react-router-dom";

const TransactionDetailsPage = () => {
  const { transactionId } = useParams();
  const [transaction, setTransaction] = useState(null);
  
  
  const [productName, setProductName] = useState("Loading..."); 
  
  const [message, setMessage] = useState("");
  const [status, setStatus] = useState("");

  const navigate = useNavigate();

  
  useEffect(() => {
    const getTransaction = async () => {
      try {
        const transactionData = await ApiService.getTransactionById(transactionId);
        setTransaction(transactionData);
        setStatus(transactionData.status);
      } catch (error) {
        showMessage(
          error.message
        );
      }
    };
    getTransaction();
  }, [transactionId]);

  
  useEffect(() => {
    if (transaction && transaction.productId) {
      
      const getProductName = async () => {
        try {
          const productData = await ApiService.getProductById(transaction.productId);
          setProductName(productData.name); 
        } catch (error) {
          setProductName("Unknown Product");
          showMessage("Error getting product details: " + error);
        }
      };
      
      getProductName();
    }
  }, [transaction]); 

  
  const handleUpdateStatus = async () => {
    try {
        await ApiService.updateTransactionStatus(transactionId, status);
        showMessage("Status updated successfully!");
        navigate("/transaction");
    } catch (error) {
        showMessage(
          error.message
        );
    }
  }

  
  const showMessage = (msg) => {
    setMessage(msg);
    setTimeout(() => {
      setMessage("");
    }, 4000);
  };

  return(
    <Layout>
      {message && <p className="message">{message}</p>}
      <div className="transaction-details-page">
        {transaction && ( 
            <>
            <div className="section-card">
                <h2>Transaction Information</h2>
                <p>Order ID: {transaction.orderId}</p>
                <p>Type: {transaction.type}</p>
                <p>Status: {transaction.status}</p>
                <p>Product: {productName}</p>
                <p>Quantity: {transaction.quantity}</p>
                <p>Date: {new Date(transaction.transactionDate).toLocaleString()}</p>
                <p>Amount: Rs {transaction.totalAmount.toFixed(2)}</p>
                
                
                
                <p>Created By: {transaction.userId}</p>

            </div>
            
            <div className="section-card transaction-staus-update">
              <label>Status: </label>
              <select 
                value={status} 
                onChange={(e)=> setStatus(e.target.value)}
              >
                  <option value="PENDING">PENDING</option>
                  <option value="COMPLETED">COMPLETED</option>
                  <option value="CANCELLED">CANCELLED</option>
              </select>
              <button onClick={handleUpdateStatus}>Update Status</button>
            </div>
            </>
        )}
      </div>
    </Layout>
  )
};

export default TransactionDetailsPage;