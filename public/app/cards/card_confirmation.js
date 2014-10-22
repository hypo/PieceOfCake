if (window.app === undefined) window.app = {};
if (window.app.cards == undefined) window.app.cards = {};

window.app.cards.ConfirmationCard = React.createClass({
  displayName: 'ConfirmationCard',
  enableCoupon: function() {
    $(this.refs.coupon_button.getDOMNode()).fadeOut();
    this.refs.coupon_input.getDOMNode().focus();
  },
  useCoupon: function () {
    var couponCode = this.refs.coupon_input.getDOMNode().value;
    app.CardStack.flashError(_("COUPON_CONFIRMING"));
    getXHR("/api/coupon/" + couponCode, function(xhr) {
      if (xhr.status != 200)
        return app.CardStack.flashError(_("TRY_AGAIN"));

      var data = JSON.parse(xhr.responseText);
      if (!data.id || !data.can_redeem)
        return app.CardStack.flashError(_("COUPON_INVALID"));

      app.CardInfoStore.setCoupon(data);
      app.CardStack.forceRender();
    });
  },
  cancelCoupon: function() {
    app.CardInfoStore.setCoupon({});
    app.CardStack.forceRender();
  },
  piecesPrice: function() {
    return this.props.price.pieces * this.props.price.pieces_qty;
  },
  render: function() {
    var discounts = "";
    if (this.props.coupon.percent_off) {
      discounts = "- " + this.props.coupon.percent_off + "%";
    } else if (this.props.coupon.discount_amount) {
      discounts = "- " + this.props.coupon.discount_amount + " NTD";
    }

    return (
      React.DOM.div({className: 'content card_confirmation'},
        React.DOM.section({},
          React.DOM.div({className: 'field shipping'},
            React.DOM.b({}, 'TO'),
            React.DOM.div({className: 'section_separator'}),
            this.props.name,
            React.DOM.br({}),
            this.props.tel,
            React.DOM.div({className: 'section_separator'}),
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
          React.DOM.div({className: 'field invoice'},
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
                  _('SHIPPING')
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
                this.props.coupon.id ?
                  React.DOM.td({className: 'item'},
                    _('DISCOUNT')
                  ) : (undefined),
                this.props.coupon.id ?
                  React.DOM.td({className: 'item_detail'},
                    discounts
                  ) : (undefined)
              ),
              React.DOM.tr({},
                React.DOM.td({colSpan: 2}, React.DOM.hr({noshade: 'noshade'}))
              ),
              React.DOM.tr({},
                React.DOM.td({className: 'item'},
                  _('TOTAL')
                ),
                React.DOM.td({className: 'item_detail'},
                  this.props.price.total + ' NTD'
                )
              )
            )
          )
        ),
        React.DOM.section({},
          React.DOM.div({className: 'field coupon'},
            (
              !this.props.coupon.code ? (
                React.DOM.div({},
                  React.DOM.div({className: 'coupon_overlay', ref: 'coupon_button', onClick: this.enableCoupon},
                    React.DOM.button({}, _('INPUT_COUPON'))
                  ),
                  React.DOM.div({className: 'coupon_input'},
                    React.DOM.input({type: 'text', className: 'coupon', ref: 'coupon_input'}),
                    React.DOM.button({className: 'use_coupon', onClick: this.useCoupon}, _('COUPON_CONFIRM'))
                  )
                )
              ) : (
                React.DOM.div({className: 'coupon_overlay', onClick: this.cancelCoupon},
                  React.DOM.button({className: 'nofloat'}, _('COUPON_USE') + this.props.coupon.code)
                )
              )
            )
          )
        ),
        React.DOM.section({},
          React.DOM.div({className: 'field estimated_shipping_date'},
            React.DOM.table({},
              React.DOM.tr({},
                React.DOM.td({className: 'item'},
                  _('ESTIMATED_SHIPPING_DATE')
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
