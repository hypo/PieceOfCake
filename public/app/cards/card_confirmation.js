if (window.app === undefined) window.app = {};
if (window.app.cards == undefined) window.app.cards = {};

window.app.cards.ConfirmationCard = React.createClass({
  displayName: 'ConfirmationCard',
  enableCoupon: function() {
    $('.coupon_overlay').fadeOut();
    document.querySelector('input.coupon').focus();
  },
  piecesPrice: function() {
    return this.props.price.pieces * this.props.price.pieces_qty;
  },
  render: function() {
    return (
      React.DOM.div({className: 'content'},
        React.DOM.section({},
          React.DOM.div({className: 'field confirm'},
            React.DOM.b({}, 'TO'),
            React.DOM.br({}),
            React.DOM.br({}),
            this.props.name,
            React.DOM.br({}),
            this.props.tel,
            React.DOM.br({}),
            React.DOM.br({}),
            this.props.zipcode,
            React.DOM.br({}),
            app.countries[this.props.country],
            React.DOM.br({}),
            this.props.city,
            this.props.area,
            React.DOM.br({}),
            this.props.addr
          )
        ),
        React.DOM.section({},
          React.DOM.div({className: 'field price'},
            React.DOM.table({},
              React.DOM.tr({},
                React.DOM.td({className: 'item'},
                  'Pieces'
                ),
                React.DOM.td({className: 'item_detail'},
                  this.piecesPrice() + ' NTD'
                )
              ),
              React.DOM.tr({},
                React.DOM.td({className: 'item'},
                  'Shipping'
                ),
                React.DOM.td({className: 'item_detail'},
                  this.props.price.shipping + ' NTD'
                )
              ),
              React.DOM.tr({},
                React.DOM.td({className: 'item'},
                  'Frame'
                ),
                React.DOM.td({className: 'item_detail'},
                  this.props.price.frame + ' NTD'
                )
              ),
              React.DOM.tr({},
                React.DOM.td({colSpan: 2}, React.DOM.hr({noshade: 'noshade'}))
              ),
              React.DOM.tr({},
                React.DOM.td({className: 'item'},
                  'Total'
                ),
                React.DOM.td({className: 'item_detail'},
                  this.props.price.total + ' NTD'
                )
              )
            )
          )
        ),
        React.DOM.section({style: {display: 'none'}},
          React.DOM.div({className: 'field coupon'},
            React.DOM.div({className: 'coupon_overlay', onClick: this.enableCoupon},
              React.DOM.button({}, '輸入 Coupon Code')
            ),
            React.DOM.table({},
              React.DOM.tr({},
                React.DOM.td({className: 'item'},
                  React.DOM.input({type: 'text', className: 'coupon', name: 'coupon'})
                ),
                React.DOM.td({className: 'item'},
                  React.DOM.button({className: 'coupon'}, '確認')
                )
              )
            )
          )
        ),
        React.DOM.section({},
          React.DOM.div({className: 'field'},
            React.DOM.table({},
              React.DOM.tr({},
                React.DOM.td({className: 'item'},
                  '預計出貨日'
                ),
                React.DOM.td({className: 'item_detail'},
                  this.props.estimated_shipping_date
                )
              )
            )
          )
        )
      )
    );
  }
});
