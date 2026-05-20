import React, { useState, useEffect } from "react";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";
import { useNavigate } from "react-router-dom";
import PaginationComponent from "../component/PaginationComponent";

const TransactionsPage = () => {
  
  const [allTransactions, setAllTransactions] = useState([]);

  const [transactions, setTransactions] = useState([]);
  const [message, setMessage] = useState("");
  
  const [filter, setFilter] = useState("");

  const [productMap, setProductMap] = useState(new Map());

  const navigate = useNavigate();

  
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const itemsPerPage = 10;

  
  useEffect(() => {
    const getInitialData = async () => {
      try {
        
        const [transactionData, productData] = await Promise.all([
          ApiService.getAllTransactions(), // <-- No filter argument
          ApiService.getAllProducts(),
        ]);

        
        const newProductMap = new Map();
        productData.forEach((product) => {
          newProductMap.set(product.id, product.name);
        });
        setProductMap(newProductMap);

        
        const sortedData = transactionData.sort((a, b) => {
          const dateA = new Date(a.transactionDate);
          const dateB = new Date(b.transactionDate);
          return dateB - dateA;
        });

        
        setAllTransactions(sortedData);
      } catch (error) {
        showMessage(error.message);
      }
    };

    getInitialData();
  }, []);
  
  useEffect(() => {
    
    const searchLower = filter.toLowerCase();

    
    const filteredData = allTransactions.filter((transaction) => {
      
      const productName = productMap.get(transaction.productId) || "";

      return (
        transaction.type.toLowerCase().includes(searchLower) ||
        transaction.status.toLowerCase().includes(searchLower) ||
        String(transaction.quantity).includes(searchLower) ||
        productName.toLowerCase().includes(searchLower)
      );
    });
    
    
    if (currentPage !== 1 && filter) {
        setCurrentPage(1);
        return; 
    }
    
    
    setTotalPages(Math.ceil(filteredData.length / itemsPerPage));
    setTransactions(
      filteredData.slice(
        (currentPage - 1) * itemsPerPage,
        currentPage * itemsPerPage
      )
    );
  }, [allTransactions, filter, currentPage, productMap]); 

  
  const showMessage = (msg) => {
    setMessage(msg);
    setTimeout(() => {
      setMessage("");
    }, 4000);
  };

  

  
  const navigateToTransactionDetailsPage = (transactionId) => {
    navigate(`/transaction/${transactionId}`);
  };

  return (
    <Layout>
      {message && <p className="message">{message}</p>}
      <div className="transactions-page">
        <div className="transactions-header">
          <h1>Transactions</h1>
          <div className="transaction-search">
            <input
              placeholder="Search Transactions..."
              value={filter}
              onChange={(e) => setFilter(e.target.value)}
              type="text"
            />
            
          </div>
        </div>

        {transactions && (
          <table className="transactions-table">
            <thead>
              <tr>
                <th>TYPE</th>
                <th>STATUS</th>
                <th>QUANTITY</th>
                <th>PRODUCT NAME</th>
                <th>DATE</th>
                <th>ACTIONS</th>
              </tr>
            </thead>
            <tbody>
              {transactions.map((transaction) => (
                <tr key={transaction.orderId}>
                  <td>{transaction.type}</td>
                  <td>{transaction.status}</td>
                  <td>{transaction.quantity}</td>
                  <td>
                    {productMap.get(transaction.productId) ||
                      transaction.productId}
                  </td>
                  <td>
                    {new Date(transaction.transactionDate).toLocaleString()}
                  </td>
                  <td>
                    <button
                      onClick={() =>
                        navigateToTransactionDetailsPage(transaction.orderId)
                      }
                    >
                      View Details
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <PaginationComponent
        currentPage={currentPage}
        totalPages={totalPages}
        onPageChange={setCurrentPage}
      />
    </Layout>
  );
};
export default TransactionsPage;