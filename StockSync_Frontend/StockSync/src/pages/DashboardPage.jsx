import React, { useState, useEffect } from "react";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";
import { useLocation, useNavigate } from "react-router-dom"; 
import { toast } from 'react-toastify'; 
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer
} from "recharts";

const DashboardPage = () => {
  const [selectedMonth, setSelectedMonth] = useState(new Date().getMonth() + 1);
  const [selectedYear, setSelectedYear] = useState(new Date().getFullYear());
  
  
  const [selectedData, setSelectedData] = useState("amount"); 
  
  const [transactionData, setTransactionData] = useState([]); 
  const [message, setMessage] = useState("");
  const location = useLocation(); 
  const navigate = useNavigate();
    
  
  useEffect(() => {
    if (location.state?.message) {
      toast.success(location.state.message);
      navigate('.', { replace: true, state: {} });
    }
  }, [location.state, navigate]);

  
  
  useEffect(() => {
    const fetchData = async () => {
      try {
        
        const transactionResponse = await ApiService.getAllTransactions();
        
        console.log("Transactions from backend:", transactionResponse);
        
        
        setTransactionData(
          transformTransactionData(
            transactionResponse, 
            selectedMonth,
            selectedYear
          )
        );

      } catch (error) {
        
        showMessage(error.message || "Error getting transactions");
        console.error(error);
      }
    };
    fetchData();
  }, [selectedMonth, selectedYear]); 

  
  
  const transformTransactionData = (transactions, month, year) => {
    const dailyData = {};
    const daysInMonths = new Date(year, month, 0).getDate();
    
    for (let day = 1; day <= daysInMonths; day++) {
      dailyData[day] = {
        day,
        count: 0,
        quantity: 0,
        amount: 0, 
      };
    }
    
    transactions.forEach((transaction) => {
      
      const transactionDate = new Date(transaction.transactionDate);
      const transactionMonth = transactionDate.getMonth() + 1;
      const transactionYear = transactionDate.getFullYear();

      if (transactionMonth === month && transactionYear === year && transaction.status === "COMPLETED") {
        const day = transactionDate.getDate();
        dailyData[day].count += 1;
        
        
        dailyData[day].quantity += transaction.quantity;
        
        
        dailyData[day].amount += transaction.totalAmount; 
      }
    });
    return Object.values(dailyData);
  };

  const handleMonthChange = (e) => {
    setSelectedMonth(parseInt(e.target.value, 10));
  };

  const handleYearChange = (e) => {
    setSelectedYear(parseInt(e.target.value, 10));
  };

  const showMessage = (msg) => {
    
    if (toast) {
        toast.error(msg);
    } else {
        setMessage(msg);
        setTimeout(() => {
          setMessage("");
        }, 4000);
    }
  };

  return (
    <Layout>
      {message && <div className="message">{message}</div>}
      <div className="dashboard-page">
        <div className="button-group">
          <button onClick={() => setSelectedData("count")}>
            ToTal No Of Transactions
          </button>
          <button onClick={() => setSelectedData("quantity")}>
            Product Quantity
          </button>
          
          
          <button onClick={() => setSelectedData("amount")}>
            Amount
          </button>

        </div>

        
        <div className="dashboard-content">
          <div className="filter-section">
            <label htmlFor="month-select">Select Month:</label>
            <select id="month-select" value={selectedMonth} onChange={handleMonthChange}>
              {Array.from({ length: 12 }, (_, i) => (
                <option key={i + 1} value={i + 1}>
                  {new Date(0, i).toLocaleString("default", { month: "long" })}
                </option>
              ))}
            </select>

            <label htmlFor="year-select">Select Year:</label>
            <select id="year-select" value={selectedYear} onChange={handleYearChange}>
              {Array.from({ length: 5 }, (_, i) => {
                const year = new Date().getFullYear() - i;
                return (
                  <option key={year} value={year}>
                    {year}
                  </option>
                );
              })}
            </select>
          </div>

          <div className="chart-section">
            <div className="chart-container">
                <h3>Daily Transactions</h3>
                <ResponsiveContainer width="100%" height={400}>
                    <LineChart data={transactionData}>
                        <CartesianGrid strokeDasharray="3 3"/>
                        <XAxis dataKey="day" label={{value: "Day", position: "insideBottomRight", offset: -5}}/>
                        <YAxis/>
                        <Tooltip/>
                        <Legend/>
                        <Line type={"monotone"}
                        dataKey={selectedData} 
                        stroke="#008080"
                        fillOpacity={0.3}
                        fill="#008080"
                        />
                    </LineChart>
                </ResponsiveContainer>
            </div>
          </div>

        </div>
      </div>
    </Layout>
  );
};
export default DashboardPage;