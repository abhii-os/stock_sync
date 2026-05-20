
const fs = require('fs');
const path = require('path');
const filePath = path.join(__dirname, 'src', 'data', 'transactions.json');
const data = JSON.parse(fs.readFileSync(filePath, 'utf8'));

data.transactions = data.transactions.filter(t => t.createdAt && t.transactionType && t.status && t.product && t.user);

fs.writeFileSync(filePath, JSON.stringify(data, null, 2));
console.log('Cleaned transactions.json. Only full-format transactions remain.');
