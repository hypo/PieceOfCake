if (window.app === undefined) window.app = {};

var CardInfoStore = function() {
  this.data = {
    frame_qty: 0
  };

  this.pricingStrategy = {};
  this.coupon = {};
}

CardInfoStore.prototype.setCoupon = function(coupon) {
  this.coupon = coupon;
}

CardInfoStore.prototype.setPricingStrategy = function(newPricingStrategy) {
  // this is only used for cilent-side rendering
  // server will always validate if the latest pricing streategy is applied
  this.pricingStrategy = newPricingStrategy;
}

CardInfoStore.prototype.setEstimatedShippingDate = function(date) {
  this.data['estimated_shipping_date'] = date;
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
  d['price']['total'] = d.price.pieces * d.price.pieces_qty + d.price.shipping + d.price.frame;
  d['coupon'] = this.coupon;

  if (this.coupon.id) {
    if (this.coupon.percent_off) {
      d['price']['total'] *= (100 - this.coupon.percent_off) / 100;
    } else if (this.coupon.discount_amount) {
      d['price']['total'] -= this.coupon.discount_amount;
    }
  }
  d['price']['total'] = Math.max(0, d['price']['total']); // make sure it's non-negative.
  return d;
}

window.app.CardInfoStore = new CardInfoStore();
