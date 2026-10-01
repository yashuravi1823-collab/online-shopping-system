const API="http://localhost:8080/api";
let user=null, cart=[];


const imageNames = {
  1: "kurti.jpg",
  2: "top.jpg",
  3: "shirt.jpg",
  4: "jeans.jpg",
  5: "mouse.jpg",
  6: "Bluetooth speaker.jpg",
  7: "facewash.jpg",
  8: "bottle.jpg",
  9: "sneakers.jpg",
  10:"frocks.jpg",
  11:"chain.jpg"
};

function imageFor(p){
  return "images/" + imageNames[p.id];
}

function post(url,data){
  return fetch(url,{method:"POST",headers:{"Content-Type":"application/x-www-form-urlencoded"},
    body:new URLSearchParams(data)}).then(r=>r.json());
}
async function register(){
  const r=await post(API+"/register",{name:regName.value,email:regEmail.value,password:regPassword.value});
  alert(r.success?"Registered! Now login.":r.message);
}
async function login(){
  const r=await post(API+"/login",{email:loginEmail.value,password:loginPassword.value});
  if(!r.success){alert(r.message);return;}
  user=r; auth.classList.add("hidden"); shop.classList.remove("hidden");
  userArea.innerHTML=`Hello, ${r.name} | <button onclick="logout()">Logout</button>`;
  loadCategories();loadProducts();loadOrders();
}
function logout(){location.reload()}
async function loadCategories(){
  const data=await fetch(API+"/categories").then(r=>r.json());
  categories.innerHTML=`<button onclick="loadProducts()">All</button>`+
    data.map(c=>`<button onclick="loadProducts(${c.id})">${c.name}</button>`).join("");
}
async function loadProducts(category){
  const url = category
    ? `${API}/products?category=${category}`
    : `${API}/products`;

  const data = await fetch(url).then(r => r.json());

  products.innerHTML = data.map(p => `
    <div class="product" onclick="openProduct(${p.id})">
      <img src="${imageFor(p)}" alt="${p.name}">
      <h3>${p.name}</h3>
      <b>₹${p.price.toFixed(2)}</b>
      <p>Tap to view product</p>
    </div>
  `).join("");
}
async function openProduct(id){

  const data = await fetch(API + "/products").then(r => r.json());

  const p = data.find(item => item.id === id);

  if(!p){
    alert("Product not found");
    return;
  }

  products.innerHTML = `
    <div class="product-detail">

      <button class="back-btn" onclick="loadProducts()">
        ← Back to Products
      </button>

      <div class="detail-box">

        <div class="detail-image">
          <img src="${imageFor(p)}" alt="${p.name}">
        </div>

        <div class="detail-info">

          <h1>${p.name}</h1>

          <div class="detail-price">
            ₹${p.price.toFixed(2)}
          </div>

          <p class="detail-description">
            ${p.description}
          </p>

          <p class="stock">
            Available stock: ${p.stock}
          </p>

          <label>Quantity:</label>

          <div class="quantity-box">
            <button onclick="changeDetailQty(-1)">−</button>
            <span id="detailQty">1</span>
            <button onclick="changeDetailQty(1)">+</button>
          </div>

          <button class="add-detail-btn"
                  onclick="addDetailToCart(${p.id})">
            Add to Cart
          </button>

          <button class="buy-detail-btn"
                  onclick="buyNow(${p.id})">
            Buy Now
          </button>

        </div>

      </div>
    </div>
  `;

  window.currentProduct = p;
  window.detailQuantity = 1;
}
function changeDetailQty(change){

  let qty = window.detailQuantity || 1;

  qty += change;

  if(qty < 1) qty = 1;

  if(window.currentProduct && qty > window.currentProduct.stock){
    qty = window.currentProduct.stock;
  }

  window.detailQuantity = qty;

  document.getElementById("detailQty").textContent = qty;
}
function addToCart(p){
  const x=cart.find(i=>i.id===p.id); if(x)x.qty++; else cart.push({...p,qty:1}); renderCart();
}
function renderCart(){
  cartDiv=document.getElementById("cart");
  cartDiv.innerHTML=cart.length?cart.map(i=>`<div>${i.name} × ${i.qty} — ₹${(i.price*i.qty).toFixed(2)}</div>`).join(""):"Cart is empty";
}
async function checkout(){
  if(!cart.length)return alert("Cart is empty");
  const mode=prompt("Payment mode: enter COD, UPI or CARD","COD");
  if(!["COD","UPI","CARD"].includes((mode||"").toUpperCase()))return alert("Invalid payment mode");
  const items=cart.map(i=>`${i.id}:${i.qty}`).join(",");
  const r=await post(API+"/order/create",{userId:user.userId,paymentMode:mode.toUpperCase(),items});
  alert(r.success?`Order #${r.orderId} placed. Delivery by ${r.expectedDelivery}`:r.message);
  if(r.success){cart=[];renderCart();loadProducts();loadOrders();}
}
async function loadOrders(){
  const data=await fetch(`${API}/orders?userId=${user.userId}`).then(r=>r.json());
  orders.innerHTML=data.length?data.map(o=>`
    <div class="order">
      <b>Order #${o.id}</b><br>
      Payment: ${o.paymentMode}<br>
      Amount: ₹${o.totalAmount.toFixed(2)}<br>
      Ordered: ${o.orderDate}<br>
      Expected delivery: ${o.expectedDelivery}<br>
      Status: <b>${o.status}</b>
      ${["PLACED","SHIPPED","OUT_FOR_DELIVERY"].includes(o.status)?`<br><button onclick="cancelOrder(${o.id})">Cancel order</button>`:""}
      <small>Cancellation is allowed only within 2 days of ordering.</small>
    </div>`).join(""):"No orders yet";
}
async function cancelOrder(id){
  if(!confirm("Cancel this order?"))return;
  const r=await post(API+"/order/cancel",{orderId:id,userId:user.userId});
  alert(r.message);loadOrders();
}
function addDetailToCart(id) {
  const p = window.currentProduct;

  if (!p) {
    alert("Product not found");
    return;
  }

  const qty = window.detailQuantity || 1;

  const existing = cart.find(item => item.id === id);

  if (existing) {
    existing.qty += qty;
  } else {
    cart.push({
      ...p,
      qty: qty
    });
  }

  renderCart();

  alert(p.name + " added to cart!");
}


function buyNow(id) {
  const p = window.currentProduct;

  if (!p) {
    alert("Product not found");
    return;
  }

  addDetailToCart(id);

  setTimeout(function() {
    checkout();
  }, 200);
}