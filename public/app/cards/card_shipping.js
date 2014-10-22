if (window.app === undefined) window.app = {};
if (window.app.cards == undefined) window.app.cards = {};

window.app.cards.ShippingCard = React.createClass({
  displayName: 'ShippingCard',
  render: function() {
    return (
      React.DOM.div({className: 'content card_shipping'},
        React.DOM.section({},
          React.DOM.div({className: 'field'},
            React.DOM.input({type: 'text', name: 'name', placeholder: _('RECEIVER'), defaultValue: this.props.name})
          )
        ),
        React.DOM.section({},
          React.DOM.div({className: 'field'},
            React.DOM.input({type: 'text', name: 'tel', placeholder: _('RECEIVER_PHONE'), defaultValue: this.props.tel})
          )
        ),
        React.DOM.section({},
          React.DOM.div({className: 'field'},
            React.DOM.select({name: 'country'},
              Object.keys(app.countries).map(function(key) {
                var props = {};
                if (key == this.props.country) {
                  props['selected'] = 'selected';
                }
                props['value'] = key

                return React.DOM.option(props, app.countries[key]);
              }.bind(this))
            )
          )
        ),
        React.DOM.section({},
          React.DOM.div({className: 'field'},
            React.DOM.input({type: 'text', name: 'city', placeholder: _('CITY'), defaultValue: this.props.city})
          )
        ),
        React.DOM.section({},
          React.DOM.div({className: 'field half'},
            React.DOM.input({type: 'text', className: 'half', name: 'area', placeholder: _('AREA'), defaultValue: this.props.area})
          ),
          React.DOM.div({className: 'field half'},
            React.DOM.input({type: 'number', className: 'half', name: 'zipcode', placeholder: _('郵遞區號'), defaultValue: this.props.zipcode})
          )
        ),
        React.DOM.section({},
          React.DOM.div({className: 'field'},
            React.DOM.textarea({name: 'addr', rows: 5, placeholder: _('ADDRESS'), defaultValue: this.props.addr})
          )
        )
      )
    );
  }
});
