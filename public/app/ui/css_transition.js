if (window.app === undefined) window.app = {};
if (window.ext === undefined) window.ext = {};

var TICK=17;
var NO_EVENT_TIMEOUT = 5000;

window.app.reverseAnimation = false;

window.ext.CSSTransitionGroupChild = React.createClass({
  displayName: "ext.CSSTransitionGroupChild",
  transition: function(animationType, finishCallback) {
    var animationName = this.props.name;
    if (app.reverseAnimation) {
      animationName += "-reverse";
    }

    var node = this.getDOMNode();
    var className = animationName + '-' + animationType;
    var activeClassName = className + '-active';
    var noEventTimeout = null;

    var endListener = function() {
      node.classList.remove(className);
      node.classList.remove(activeClassName);

      node.removeEventListener("webkitTransitionEnd", endListener, false);
      node.removeEventListener("webkitAnimationEnd", endListener, false);

      // Usually this optional callback is used for informing an owner of
      // a leave animation and telling it to remove the child.
      finishCallback && finishCallback();
    };

    node.addEventListener("webkitTransitionEnd", endListener, false);
    node.addEventListener("webkitAnimationEnd", endListener, false);

    node.classList.add(className);

    // Need to do this to actually trigger a transition.
    this.queueClass(activeClassName);
  },

  queueClass: function(className) {
    this.classNameQueue.push(className);

    if (this.props.runNextTick) {
      this.props.runNextTick(this.flushClassNameQueue);
      return;
    }

    if (!this.timeout) {
      this.timeout = setTimeout(this.flushClassNameQueue, TICK);
    }
  },

  flushClassNameQueue: function() {
    if (this.isMounted()) {
      this.classNameQueue.forEach(function(n) {
        this.getDOMNode().classList.add(n);
      }.bind(this));
    }
    this.classNameQueue.length = 0;
    this.timeout = null;
  },

  componentWillMount: function() {
    this.classNameQueue = [];
  },

  componentWillUnmount: function() {
    if (this.timeout) {
      clearTimeout(this.timeout);
    }
  },

  componentWillEnter: function(done) {
    if (this.props.enter) {
      this.transition('enter', done);
    } else {
      done();
    }
  },

  componentWillLeave: function(done) {
    if (this.props.leave) {
      this.transition('leave', done);
    } else {
      done();
    }
  },

  render: function() {
    return React.Children.only(this.props.children);
  }
});

window.ext.CSSTransitionGroup = React.createClass({
  displayName: "ext.CSSTransitionGroup",
  propTypes: {
    transitionName: React.PropTypes.string.isRequired,
    transitionEnter: React.PropTypes.bool,
    transitionLeave: React.PropTypes.bool
  },

  getDefaultProps: function() {
    return {
      transitionEnter: true,
      transitionLeave: true
    };
  },

  _wrapChild: function(child) {
    // We need to provide this childFactory so that
    // ReactCSSTransitionGroupChild can receive updates to name, enter, and
    // leave while it is leaving.
    return ext.CSSTransitionGroupChild(
      {
        name: this.props.transitionName,
        enter: this.props.transitionEnter,
        leave: this.props.transitionLeave
      },
      child
    );
  },

  render: function() {
    return this.transferPropsTo(
      React.addons.TransitionGroup(
        {childFactory: this._wrapChild},
        this.props.children
      )
    );
  }
});
