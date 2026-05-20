const jsonServer = require('json-server');
const server = jsonServer.create();
const middlewares = jsonServer.defaults();
const cors = require('cors');
const path = require('path');
const fs = require('fs');
const low = require('lowdb');
const FileSync = require('lowdb/adapters/FileSync');
const { FAKE_JWT, apiResponses } = require('./responses');

const API_PREFIX = '/api/v1';


server.use(cors());
server.use(middlewares);
server.use(jsonServer.bodyParser);


const checkAuth = (req, res, next) => {
  const header = req.header('Authorization');
  if (!header) return res.status(401).jsonp(apiResponses.unauthorized);
  const token = header.replace('Bearer ', '');
  if (token === FAKE_JWT) next();
  else return res.status(403).jsonp(apiResponses.forbidden);
};


const dataDir = path.join(__dirname, 'data');
const dbMap = {};
fs.readdirSync(dataDir).forEach(file => {
  if (file.endsWith('.json')) {
    const resourceName = file.replace('.json', '');
    const adapter = new FileSync(path.join(dataDir, file));
    dbMap[resourceName] = low(adapter);
    if (!dbMap[resourceName].has(resourceName).value()) {
      dbMap[resourceName].set(resourceName, []).write();
    }
  }
});

const db = {};
Object.keys(dbMap).forEach(resource => {
  db[resource] = dbMap[resource].get(resource).value() || [];
});

const router = jsonServer.router(db, { id: 'id' });


server.post(`${API_PREFIX}/auth/login`, (req, res) => {
  const { email, password } = req.body;
  const users = dbMap.users.get('users').value() || [];
  const user = users.find(
    u => u.email === email && (u.password === password || u.password.startsWith(`hashed_${password}_`))
  );
  if (user) {
    const { password, ...userSafe } = user;
    res.jsonp({ ...userSafe, jwt: FAKE_JWT });
  } else {
    res.status(401).jsonp(apiResponses.loginFailure);
  }
});

server.post(`${API_PREFIX}/auth/register`, (req, res) => {
  const { name, email, password, phoneNumber } = req.body;
  if (!name || !email || !password) {
    return res.status(400).jsonp({ message: 'Name, email, and password are required.' });
  }
  if (phoneNumber && !/^\d{10,15}$/.test(phoneNumber)) {
    return res.status(400).jsonp({ message: 'Phone number must be between 10 and 15 digits.' });
  }
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) {
    return res.status(400).jsonp({ message: 'Please provide a valid email address.' });
  }

  const usersDb = dbMap.users.get('users');
  const existingUser = usersDb.find({ email }).value();
  if (existingUser) {
    return res.status(409).jsonp({ message: 'An account with this email already exists.' });
  }

  const newUser = {
    id: `${Date.now()}-${Math.floor(Math.random() * 1000)}`,
    name,
    email,
    password: `hashed_${password}_${Date.now()}`,
    phoneNumber: phoneNumber || '',
    role: 'MANAGER'
  };

  usersDb.push(newUser).write();
  router.db.set('users', usersDb.value());
  const { password: _, ...userSafe } = newUser;
  return res.status(201).jsonp({ ...userSafe, jwt: FAKE_JWT });
});


server.use((req, res, next) => {
  const publicRoutes = [`${API_PREFIX}/auth/login`, `${API_PREFIX}/auth/register`];
  if (publicRoutes.includes(req.path)) next();
  else checkAuth(req, res, next);
});


server.get(`${API_PREFIX}/users`, (req, res) => {
  const users = dbMap.users.get('users').value() || [];
  const safeUsers = users.map(({ password, ...rest }) => rest);
  res.jsonp(safeUsers);
});

server.get(`${API_PREFIX}/products`, (req, res) => {
  const products = dbMap.products.get('products').value() || [];
  res.jsonp(products);
});


server.post(`${API_PREFIX}/transactions/sell`, (req, res) => {
  const { productId, userId, quantity, supplierId, ...rest } = req.body;

  const product = dbMap.products.get('products').find({ id: String(productId) }).value();
  const user = dbMap.users.get('users').find({ id: String(userId) }).value();
  const supplier = supplierId ? dbMap.suppliers.get('suppliers').find({ id: String(supplierId) }).value() : null;

  if (!product || !user) {
    return res.status(404).jsonp({ error: 'Product or User not found' });
  }

  if (product.stockQuantity < quantity) {
    return res.status(400).jsonp({ message: `Sorry, only ${product.stockQuantity} units of '${product.name}' are available in stock. Please adjust your quantity and try again.` });
  }

  const { password, ...userSafe } = user;
  const transactionsDb = dbMap.transactions.get('transactions');
  const newTransaction = {
    id: `${Date.now()}-${Math.floor(Math.random() * 1000)}`,
    transactionType: 'SELL',
    status: 'PENDING',
    product,
    user: userSafe,
    supplier,
    totalProducts: quantity,
    totalPrice: product.price * quantity,
    createdAt: new Date().toISOString(),
    ...rest,
  };

  transactionsDb.push(newTransaction).write();
  router.db.set('transactions', transactionsDb.value());

  return res.status(201).jsonp(newTransaction);
});


server.post(`${API_PREFIX}/transactions/purchase`, (req, res) => {
  const { productId, userId, supplierId, quantity, ...rest } = req.body;
  const product = dbMap.products.get('products').find({ id: String(productId) }).value();
  const user = dbMap.users.get('users').find({ id: String(userId) }).value();
  const supplier = supplierId ? dbMap.suppliers.get('suppliers').find({ id: String(supplierId) }).value() : null;

  if (!product || !user || !supplier) {
    return res.status(404).jsonp({ error: 'Product, User, or Supplier not found' });
  }

  const { password, ...userSafe } = user;
  const transactionsDb = dbMap.transactions.get('transactions');
  const newTransaction = {
    id: `${Date.now()}-${Math.floor(Math.random() * 1000)}`,
    transactionType: 'PURCHASE',
    status: 'PENDING',
    product,
    user: userSafe,
    supplier,
    totalProducts: quantity,
    totalPrice: product.price * quantity,
    createdAt: new Date().toISOString(),
    ...rest,
  };

  transactionsDb.push(newTransaction).write();
  router.db.set('transactions', transactionsDb.value());

  return res.status(201).jsonp(newTransaction);
});


server.use((req, res, next) => {
  const resource = req.path.replace(`${API_PREFIX}/`, '').split('/')[0];
  if (!dbMap[resource] || req.method === 'GET') return next();

  
  if (resource === 'transactions' && req.method === 'POST') {
    
    if (req.path === `${API_PREFIX}/transactions`) {
      return res.status(403).jsonp({ error: 'Direct transaction creation is not allowed. Use /transactions/sell or /transactions/purchase.' });
    }
    
    if (!req.body.createdAt || !req.body.transactionType || !req.body.status || !req.body.product || !req.body.user) {
      return res.status(400).jsonp({ error: 'Incomplete transaction object. Use /transactions/sell or /transactions/purchase endpoints.' });
    }
  }

  const id = req.path.split('/').pop();
  const data = req.body;
  const resourceChain = dbMap[resource].get(resource);

  if (resource === 'transactions' && req.method === 'PATCH' && data.status) {
    const oldTransaction = resourceChain.find({ id: String(id) }).value();
    const newStatus = data.status;

    if (!oldTransaction) {
      return res.status(404).jsonp({ error: `Transaction with id ${id} not found` });
    }

    if (newStatus !== oldTransaction.status) {
      const productsDb = dbMap.products.get('products');
      const product = productsDb.find({ id: String(oldTransaction.product.id) }).value();
      let newStockQuantity = product.stockQuantity;

      if (oldTransaction.status === 'COMPLETED') {
        newStockQuantity = oldTransaction.transactionType === 'SELL'
          ? product.stockQuantity + oldTransaction.totalProducts
          : product.stockQuantity - oldTransaction.totalProducts;
      }

      if (newStatus === 'COMPLETED') {
        newStockQuantity = oldTransaction.transactionType === 'SELL'
          ? product.stockQuantity - oldTransaction.totalProducts
          : product.stockQuantity + oldTransaction.totalProducts;
      }

      productsDb.find({ id: String(product.id) }).assign({ stockQuantity: newStockQuantity }).write();
      router.db.set('products', productsDb.value());
    }

    const updatedTransaction = resourceChain.find({ id: String(id) }).assign(data).write();
    router.db.set('transactions', resourceChain.value());
    return res.jsonp(updatedTransaction);
  }

  if (req.method === 'POST') {
    if (!data.id) data.id = `${Date.now()}-${Math.floor(Math.random() * 1000)}`;
    resourceChain.push(data).write();
    router.db.set(resource, resourceChain.value());
    return res.status(201).jsonp(data);
  }

  const item = resourceChain.find({ id: String(id) });
  if (!item.value()) {
    return res.status(404).jsonp({ error: `${resource} with id ${id} not found` });
  }

  if (req.method === 'PATCH') {
    const updatedItem = item.assign(data).write();
    router.db.set(resource, resourceChain.value());
    return res.jsonp(updatedItem);
  }

  if (req.method === 'PUT') {
    const replacedItem = item.assign({ ...data, id: String(id) }).write();
    router.db.set(resource, resourceChain.value());
    return res.jsonp(replacedItem);
  }

  if (req.method === 'DELETE') {
    resourceChain.remove({ id: item.value().id }).write();
    router.db.set(resource, resourceChain.value());
    return res.status(204).jsonp({});
  }
});


server.use(API_PREFIX, router);


server.listen(3000, () => {
  console.log('JSON Server is running on port 3000');
});
