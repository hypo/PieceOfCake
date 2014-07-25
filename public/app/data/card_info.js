if (window.app === undefined) window.app = {};

var CardInfoStore = function() {
  this.data = {
    name: "鄂雅玲",
    tel: "0983765046",
    country: "台灣",
    city: "台北市",
    area: "中正區",
    addr: "忠孝東路二段39巷2弄14號2樓",
    zipcode: "10423",
    frame_qty: 0,
    cardno: "4311952222222222",
    expiry: "0814",
    cvv: "222",
    estimated_shipping_date: "2014-02-13"
  };

  this.pricingStrategy = {};
}

CardInfoStore.prototype.setPricingStrategy = function(newPricingStrategy) {
  // this is only used for cilent-side rendering
  // server will always validate if the latest pricing streategy is applied
  this.pricingStrategy = newPricingStrategy;
}

CardInfoStore.prototype.update = function(data) {
  for(var i=0;i<data.length;i++) {
    var k = data[i][0];
    var v = data[i][1];
    this.data[k] = v;
  }
}

CardInfoStore.prototype.getData = function() {
  return this._addPricingInfo(this.data)
}

CardInfoStore.prototype.setData = function(data) {
  this.data = data;
}

CardInfoStore.prototype._addPricingInfo = function(data) {
  // make a deep copy
  var d = JSON.parse(JSON.stringify(data));

  d['price'] = JSON.parse(JSON.stringify(this.pricingStrategy));
  d['price']['frame'] = d.price.frame * d.frame_qty;
  d['price']['total'] = d.price.pieces + d.price.shipping + d.price.frame;

  return d;
}

window.app.CardInfoStore = new CardInfoStore();
