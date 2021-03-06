define(function (require, exports, module) {

    /*!
     * typeahead.js 0.10.5
     * https://github.com/twitter/typeahead.js
     * Copyright 2013-2014 Twitter, Inc. and other contributors; Licensed MIT
     */

    module.exports = function ($) {
        var _ = function () {  // @ 功用函数
            "use strict";
            return {
                isMsie: function () {  // @ 通过用户代理信息监测用户代理类型是否是 IE，返回值是 false 或 IE 版本
                    return /(msie|trident)/i.test(navigator.userAgent) ? navigator.userAgent.match(/(msie |rv:)(\d+(.\d+)?)/i)[2] : false;
                },
                isBlankString: function (str) {
                    return !str || /^\s*$/.test(str);
                },
                escapeRegExChars: function (str) {
                    return str.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&");
                },
                isString: function (obj) {  // @ jQuery 中没有写这个 isString()
                    return typeof obj === "string";
                },
                isNumber: function (obj) {  // @ 没有使用 jQuery 中的 isNumeric()，因为前者对于数字或数字字符串都返回 true
                    return typeof obj === "number";
                },
                isArray: $.isArray,
                isFunction: $.isFunction,
                isObject: $.isPlainObject,
                isUndefined: function (obj) {
                    return typeof obj === "undefined";
                },
                toStr: function toStr(s) {
                    return _.isUndefined(s) || s === null ? "" : s + "";
                },
                bind: $.proxy,  // @ 使用 jQuery 的 $.proxy()，兼容不支持 Function.prototype.bind() 的浏览器
                each: function (collection, cb) {  // @ _.each() 与 $.each() 的区别是前者 callback 函数接受参数顺序是 value-key（后者是key-value）
                    $.each(collection, reverseArgs);  // @ 于是 _.each() 的回调函数在只写一个参数时指的是 value
                    function reverseArgs(index, value) {
                        return cb(value, index);
                    }
                },
                map: $.map,  // @ jQuery 的 $.map()，在数组（ArrayLike）或对象的每一项上应用一个函数，将结果组合成数组返回。相比原生的 Array.prototype.map()，$.map() 还可应用于对象。
                filter: $.grep,
                every: function (obj, test) {
                    var result = true;
                    if (!obj) {
                        return result;
                    }
                    $.each(obj, function (key, val) {
                        if (!(result = test.call(null, val, key, obj))) {
                            return false;
                        }
                    });
                    return !!result;
                },
                some: function (obj, test) {
                    var result = false;
                    if (!obj) {
                        return result;
                    }
                    $.each(obj, function (key, val) {
                        if (result = test.call(null, val, key, obj)) {
                            return false;
                        }
                    });
                    return !!result;
                },
                mixin: $.extend,  // @ 原来如此
                getUniqueId: function () {
                    var counter = 0;
                    return function () {
                        return counter++;
                    };
                }(),
                templatify: function templatify(obj) {
                    return $.isFunction(obj) ? obj : template;
                    function template() {
                        return String(obj);
                    }
                },
                defer: function (fn) {
                    setTimeout(fn, 0);  // @ 将 js 代码切块，使浏览器有机会在此时插入其他操作
                },
                debounce: function (func, wait, immediate) {
                    var timeout, result;
                    return function () {
                        var context = this, args = arguments, later, callNow;
                        later = function () {
                            timeout = null;
                            if (!immediate) {
                                result = func.apply(context, args);
                            }
                        };
                        callNow = immediate && !timeout;
                        clearTimeout(timeout);
                        timeout = setTimeout(later, wait);
                        if (callNow) {
                            result = func.apply(context, args);
                        }
                        return result;
                    };
                },
                throttle: function (func, wait) {
                    var context, args, timeout, result, previous, later;
                    previous = 0;
                    later = function () {
                        previous = new Date();
                        timeout = null;
                        result = func.apply(context, args);
                    };
                    return function () {
                        var now = new Date(), remaining = wait - (now - previous);
                        context = this;
                        args = arguments;
                        if (remaining <= 0) {
                            clearTimeout(timeout);
                            timeout = null;
                            previous = now;
                            result = func.apply(context, args);
                        } else if (!timeout) {
                            timeout = setTimeout(later, remaining);
                        }
                        return result;
                    };
                },
                noop: function () {
                }
            };
        }();
        var html = function () {
            return {
                wrapper: '<span class="twitter-typeahead"></span>',
                dropdown: '<span class="tt-dropdown-menu"></span>',
                dataset: '<div class="tt-dataset-%CLASS%"></div>',
                suggestions: '<span class="tt-suggestions"></span>',
                suggestion: '<div class="tt-suggestion"></div>'
            };
        }();
        var css = function () {
            "use strict";
            var css = {
                wrapper: {
                    position: "relative",  // @ 保证 wrapper 的 position 不是 static，这样其子元素的 absolute 定位才能起到预期作用
                    display: "inline-block"
                },
                hint: {  // @ 输入框的底层
                    position: "absolute",  // @ 不占位
                    top: "0",
                    left: "0",
                    borderColor: "transparent",
                    boxShadow: "none",
                    opacity: "1"
                },
                input: {  // @ 输入框的顶层
                    position: "relative",  // @ 占位
                    verticalAlign: "top",
                    backgroundColor: "transparent"
                },
                inputWithNoHint: {
                    position: "relative",
                    verticalAlign: "top"
                },
                dropdown: {
                    position: "absolute",  // @ 不占位
                    top: "100%",  // @ 完全向下溢出，其顶部紧贴 wrapper 的底部
                    left: "0",  // @ 左侧对齐
                    zIndex: "100",
                    display: "none"  // @ 默认不可见
                },
                suggestions: {
                    display: "block"
                },
                suggestion: {
                    whiteSpace: "nowrap",
                    cursor: "pointer"
                },
                suggestionChild: {
                    whiteSpace: "normal"
                },
                ltr: {
                    left: "0",
                    right: "auto"
                },
                rtl: {
                    left: "auto",
                    right: " 0"
                }
            };
            if (_.isMsie()) {
                _.mixin(css.input, {
                    backgroundImage: "url(data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7)"
                });
            }
            if (_.isMsie() && _.isMsie() <= 7) {
                _.mixin(css.input, {
                    marginTop: "-1px"
                });
            }
            return css;
        }();
        var EventBus = function () {
            "use strict";
            var namespace = "typeahead:";

            function EventBus(o) {
                if (!o || !o.el) {  // @ el is short for element
                    $.error("EventBus initialized without el");
                }
                this.$el = $(o.el);
            }

            _.mixin(EventBus.prototype, {
                trigger: function (type) {
                    var args = [].slice.call(arguments, 1);
                    this.$el.trigger(namespace + type, args);  // @ 这个 trigger 是 jQuery 事件对象的 trigger 方法
                }
            });
            return EventBus;
        }();
        var EventEmitter = function () {
            "use strict";
            var splitter = /\s+/, nextTick = getNextTick();
            return {
                onSync: onSync,
                onAsync: onAsync,
                off: off,
                trigger: trigger
            };
            function on(method, types, cb, context) {
                var type;
                if (!cb) {
                    return this;
                }
                types = types.split(splitter);
                cb = context ? bindContext(cb, context) : cb;
                this._callbacks = this._callbacks || {};
                while (type = types.shift()) {
                    this._callbacks[type] = this._callbacks[type] || {
                        sync: [],
                        async: []
                    };
                    this._callbacks[type][method].push(cb);
                }
                return this;
            }

            function onAsync(types, cb, context) {
                return on.call(this, "async", types, cb, context);
            }

            function onSync(types, cb, context) {
                return on.call(this, "sync", types, cb, context);
            }

            function off(types) {
                var type;
                if (!this._callbacks) {
                    return this;
                }
                types = types.split(splitter);
                while (type = types.shift()) {
                    delete this._callbacks[type];
                }
                return this;
            }

            function trigger(types) {  // @ Input, Dataset, Dropdown 对象中的 trigger 方法就是它了！
                var type, callbacks, args, syncFlush, asyncFlush;
                if (!this._callbacks) {
                    return this;
                }
                types = types.split(splitter);
                args = [].slice.call(arguments, 1);
                while ((type = types.shift()) && (callbacks = this._callbacks[type])) {
                    syncFlush = getFlush(callbacks.sync, this, [type].concat(args));
                    asyncFlush = getFlush(callbacks.async, this, [type].concat(args));
                    syncFlush() && nextTick(asyncFlush);
                }
                return this;
            }

            function getFlush(callbacks, context, args) {
                return flush;
                function flush() {
                    var cancelled;
                    for (var i = 0, len = callbacks.length; !cancelled && i < len; i += 1) {
                        cancelled = callbacks[i].apply(context, args) === false;
                    }
                    return !cancelled;
                }
            }

            function getNextTick() {
                var nextTickFn;
                if (window.setImmediate) {
                    nextTickFn = function nextTickSetImmediate(fn) {
                        setImmediate(function () {
                            fn();
                        });
                    };
                } else {
                    nextTickFn = function nextTickSetTimeout(fn) {
                        setTimeout(function () {
                            fn();
                        }, 0);
                    };
                }
                return nextTickFn;
            }

            function bindContext(fn, context) {  // @ TODO 为何不用 _.bind 而是又在此写了一个？
                return fn.bind ? fn.bind(context) : function () {
                    fn.apply(context, [].slice.call(arguments, 0));  // @ 此句没加 return，那么这样绑定的函数在执行时总是会返回 undefined ！
                };
            }
        }();
        var highlight = function (doc) {
            "use strict";
            var defaults = {
                node: null,
                pattern: null,
                tagName: "strong",
                className: null,
                wordsOnly: false,
                caseSensitive: false
            };
            return function hightlight(o) {  // @ !! 这里的 hightlight 是 highlight 的笔误还是另有深意？（以及下面的 hightlightTextNode ...）
                var regex;
                o = _.mixin({}, defaults, o);
                if (!o.node || !o.pattern) {
                    return;
                }
                o.pattern = _.isArray(o.pattern) ? o.pattern : [o.pattern];
                regex = getRegex(o.pattern, o.caseSensitive, o.wordsOnly);
                traverse(o.node, hightlightTextNode);  // @ 高亮 o.node 节点子树中所有文本节点的匹配文字，o.node 是原生 DOM 节点
                function hightlightTextNode(textNode) {
                    var match, patternNode, wrapperNode;
                    if (match = regex.exec(textNode.data)) {
                        wrapperNode = doc.createElement(o.tagName);  // @ 高亮方式是用标签包裹匹配文字，默认是用 strong 标签
                        o.className && (wrapperNode.className = o.className);
                        patternNode = textNode.splitText(match.index);  // @ 123 -> 1 23
                        patternNode.splitText(match[0].length);  // @ 1 2 3
                        wrapperNode.appendChild(patternNode.cloneNode(true)); // @ (2)
                        textNode.parentNode.replaceChild(wrapperNode, patternNode);  // @ 1 (2) 3，包裹完成
                    }
                    return !!match;
                }

                function traverse(el, hightlightTextNode) {
                    var childNode, TEXT_NODE_TYPE = 3;
                    for (var i = 0; i < el.childNodes.length; i++) {
                        childNode = el.childNodes[i];
                        if (childNode.nodeType === TEXT_NODE_TYPE) {
                            i += hightlightTextNode(childNode) ? 1 : 0;
                        } else {
                            traverse(childNode, hightlightTextNode);
                        }
                    }
                }
            };
            function getRegex(patterns, caseSensitive, wordsOnly) {
                var escapedPatterns = [], regexStr;
                for (var i = 0, len = patterns.length; i < len; i++) {
                    escapedPatterns.push(_.escapeRegExChars(patterns[i]));
                }
                regexStr = wordsOnly ? "\\b(" + escapedPatterns.join("|") + ")\\b" : "(" + escapedPatterns.join("|") + ")";
                return caseSensitive ? new RegExp(regexStr) : new RegExp(regexStr, "i");
            }
        }(window.document);  // @ 便于使用 window.document.createElement 方法
        var Input = function () {
            "use strict";
            var specialKeyCodeMap;
            specialKeyCodeMap = {
                9: "tab",
                27: "esc",
                37: "left",
                39: "right",
                13: "enter",
                38: "up",
                40: "down"
            };
            function Input(o) {
                var that = this, onBlur, onFocus, onKeydown, onInput;
                o = o || {};
                if (!o.input) {
                    $.error("input is missing");
                }
                onBlur = _.bind(this._onBlur, this);
                onFocus = _.bind(this._onFocus, this);
                onKeydown = _.bind(this._onKeydown, this);
                onInput = _.bind(this._onInput, this);
                this.$hint = $(o.hint);
                this.$input = $(o.input).on("blur.tt", onBlur).on("focus.tt", onFocus).on("keydown.tt", onKeydown);
                if (this.$hint.length === 0) {
                    this.setHint = this.getHint = this.clearHint = this.clearHintIfInvalid = _.noop;
                }
                if (!_.isMsie()) {
                    this.$input.on("input.tt", onInput);
                } else {
                    this.$input.on("keydown.tt keypress.tt cut.tt paste.tt", function ($e) {
                        if (specialKeyCodeMap[$e.which || $e.keyCode]) {
                            return;
                        }
                        _.defer(_.bind(that._onInput, that, $e));
                    });
                }
                this.query = this.$input.val();  // @ 初始化时输入框的内容就是 query 的值
                this.$overflowHelper = buildOverflowHelper(this.$input);  // @ TODO 用于？
            }

            Input.normalizeQuery = function (str) {
                return (str || "").replace(/^\s*/g, "").replace(/\s{2,}/g, " ");
            };
            _.mixin(Input.prototype, EventEmitter, {
                _onBlur: function onBlur() {
                    this.resetInputValue();
                    this.trigger("blurred");
                },
                _onFocus: function onFocus() {
                    this.trigger("focused");
                },
                _onKeydown: function onKeydown($e) {
                    var keyName = specialKeyCodeMap[$e.which || $e.keyCode];
                    this._managePreventDefault(keyName, $e);
                    if (keyName && this._shouldTrigger(keyName, $e)) {
                        this.trigger(keyName + "Keyed", $e);
                    }
                },
                _onInput: function onInput() {
                    this._checkInputValue();
                },
                _managePreventDefault: function managePreventDefault(keyName, $e) {
                    var preventDefault, hintValue, inputValue;
                    switch (keyName) {
                        case "tab":
                            hintValue = this.getHint();
                            inputValue = this.getInputValue();
                            preventDefault = hintValue && hintValue !== inputValue && !withModifier($e);
                            break;

                        case "up":
                        case "down":
                            preventDefault = !withModifier($e);
                            break;

                        default:
                            preventDefault = false;
                    }
                    preventDefault && $e.preventDefault();  // @ 注意学习这种用 || 或 && 短路的写法
                },
                _shouldTrigger: function shouldTrigger(keyName, $e) {
                    var trigger;
                    switch (keyName) {
                        case "tab":
                            trigger = !withModifier($e);
                            break;

                        default:
                            trigger = true;
                    }
                    return trigger;
                },
                _checkInputValue: function checkInputValue() {
                    var inputValue, areEquivalent, hasDifferentWhitespace;
                    inputValue = this.getInputValue();
                    areEquivalent = areQueriesEquivalent(inputValue, this.query);
                    hasDifferentWhitespace = areEquivalent ? this.query.length !== inputValue.length : false;
                    this.query = inputValue;
                    if (!areEquivalent) {
                        this.trigger("queryChanged", this.query);
                    } else if (hasDifferentWhitespace) {
                        this.trigger("whitespaceChanged", this.query);
                    }
                },
                focus: function focus() {
                    this.$input.focus();
                },
                blur: function blur() {
                    this.$input.blur();
                },
                getQuery: function getQuery() {
                    return this.query;
                },
                setQuery: function setQuery(query) {
                    this.query = query;
                },
                getInputValue: function getInputValue() {
                    return this.$input.val();
                },
                setInputValue: function setInputValue(value, silent) {
                    this.$input.val(value);

                    // @ 特别为stigmod更改，为了触发stigmod中的“文本合法性检查”以及“推荐栏更新”
                    this.$input.trigger('keyup');

                    silent ? this.clearHint() : this._checkInputValue();
                },
                resetInputValue: function resetInputValue() {
                    this.setInputValue(this.query, true);
                },
                getHint: function getHint() {
                    return this.$hint.val();
                },
                setHint: function setHint(value) {
                    this.$hint.val(value);
                },
                clearHint: function clearHint() {
                    this.setHint("");
                },
                clearHintIfInvalid: function clearHintIfInvalid() {
                    var val, hint, valIsPrefixOfHint, isValid;
                    val = this.getInputValue();
                    hint = this.getHint();
                    valIsPrefixOfHint = val !== hint && hint.indexOf(val) === 0;
                    isValid = val !== "" && valIsPrefixOfHint && !this.hasOverflow();
                    !isValid && this.clearHint();
                    this.clearHint();
                },
                getLanguageDirection: function getLanguageDirection() {
                    return (this.$input.css("direction") || "ltr").toLowerCase();
                },
                hasOverflow: function hasOverflow() {
                    var constraint = this.$input.width() - 2;
                    this.$overflowHelper.text(this.getInputValue());
                    return this.$overflowHelper.width() >= constraint;
                },
                isCursorAtEnd: function () {
                    var valueLength, selectionStart, range;
                    valueLength = this.$input.val().length;
                    selectionStart = this.$input[0].selectionStart;
                    if (_.isNumber(selectionStart)) {
                        return selectionStart === valueLength;
                    } else if (document.selection) {
                        range = document.selection.createRange();
                        range.moveStart("character", -valueLength);
                        return valueLength === range.text.length;
                    }
                    return true;
                },
                destroy: function destroy() {
                    this.$hint.off(".tt");
                    this.$input.off(".tt");
                    this.$hint = this.$input = this.$overflowHelper = null;
                }
            });
            return Input;
            function buildOverflowHelper($input) {
                return $('<pre aria-hidden="true"></pre>').css({
                    position: "absolute",
                    visibility: "hidden",  // @ 瞬间松了一口气
                    whiteSpace: "pre",
                    fontFamily: $input.css("font-family"),
                    fontSize: $input.css("font-size"),
                    fontStyle: $input.css("font-style"),
                    fontVariant: $input.css("font-variant"),
                    fontWeight: $input.css("font-weight"),
                    wordSpacing: $input.css("word-spacing"),
                    letterSpacing: $input.css("letter-spacing"),
                    textIndent: $input.css("text-indent"),
                    textRendering: $input.css("text-rendering"),
                    textTransform: $input.css("text-transform")
                }).insertAfter($input);
            }

            function areQueriesEquivalent(a, b) {
                return Input.normalizeQuery(a) === Input.normalizeQuery(b);
            }

            function withModifier($e) {
                return $e.altKey || $e.ctrlKey || $e.metaKey || $e.shiftKey;
            }
        }();
        var Dataset = function () {
            "use strict";
            var datasetKey = "ttDataset", valueKey = "ttValue", datumKey = "ttDatum";

            function Dataset(o) {
                o = o || {};
                o.templates = o.templates || {};
                if (!o.source) {
                    $.error("missing source");
                }
                if (o.name && !isValidName(o.name)) {
                    $.error("invalid dataset name: " + o.name);
                }
                this.query = null;
                this.highlight = !!o.highlight;
                this.name = o.name || _.getUniqueId();
                this.source = o.source;
                this.displayFn = getDisplayFn(o.display || o.displayKey);
                this.templates = getTemplates(o.templates, this.displayFn);
                this.$el = $(html.dataset.replace("%CLASS%", this.name));  // @ 作为 dataset 容器的 DOM 元素
            }

            Dataset.extractDatasetName = function extractDatasetName(el) {
                return $(el).data(datasetKey);
            };
            Dataset.extractValue = function extractDatum(el) {
                return $(el).data(valueKey);
            };
            Dataset.extractDatum = function extractDatum(el) {
                return $(el).data(datumKey);
            };
            _.mixin(Dataset.prototype, EventEmitter, {
                _render: function render(query, suggestions) {
                    if (!this.$el) {
                        return;
                    }
                    var that = this, hasSuggestions;
                    this.$el.empty();
                    hasSuggestions = suggestions && suggestions.length;
                    if (!hasSuggestions && this.templates.empty) {
                        this.$el.html(getEmptyHtml()).prepend(that.templates.header ? getHeaderHtml() : null).append(that.templates.footer ? getFooterHtml() : null);
                    } else if (hasSuggestions) {
                        this.$el.html(getSuggestionsHtml()).prepend(that.templates.header ? getHeaderHtml() : null).append(that.templates.footer ? getFooterHtml() : null);
                    }
                    this.trigger("rendered");
                    function getEmptyHtml() {
                        return that.templates.empty({
                            query: query,
                            isEmpty: true
                        });
                    }

                    function getSuggestionsHtml() {  // @ $suggestions 是 $dataset 的子元素
                        var $suggestions, nodes;
                        $suggestions = $(html.suggestions).css(css.suggestions);
                        nodes = _.map(suggestions, getSuggestionNode);
                        $suggestions.append.apply($suggestions, nodes);
                        that.highlight && highlight({  // @ 唯一使用到 highlight() 的地方
                            className: "tt-highlight",
                            node: $suggestions[0],  // @ 取出 $suggestions 这个 jQuery 对象中的原生 DOM 节点
                            pattern: query
                        });
                        return $suggestions;
                        function getSuggestionNode(suggestion) {  // @ $suggestion 是 $suggestions 的子元素
                            var $el;
                            $el = $(html.suggestion).append(that.templates.suggestion(suggestion)).data(datasetKey, that.name).data(valueKey, that.displayFn(suggestion)).data(datumKey, suggestion);
                            $el.children().each(function () {
                                $(this).css(css.suggestionChild);
                            });
                            return $el;
                        }
                    }

                    function getHeaderHtml() {
                        return that.templates.header({
                            query: query,
                            isEmpty: !hasSuggestions
                        });
                    }

                    function getFooterHtml() {
                        return that.templates.footer({
                            query: query,
                            isEmpty: !hasSuggestions
                        });
                    }
                },
                getRoot: function getRoot() {
                    return this.$el;
                },
                update: function update(query) {
                    var that = this;
                    this.query = query;
                    this.canceled = false;
                    this.source(query, render);
                    function render(suggestions) {
                        if (!that.canceled && query === that.query) {
                            that._render(query, suggestions);
                        }
                    }
                },
                cancel: function cancel() {
                    this.canceled = true;
                },
                clear: function clear() {
                    this.cancel();
                    this.$el.empty();
                    this.trigger("rendered");
                },
                isEmpty: function isEmpty() {
                    return this.$el.is(":empty");
                },
                destroy: function destroy() {
                    this.$el = null;
                }
            });
            return Dataset;
            function getDisplayFn(display) {
                display = display || "value";
                return _.isFunction(display) ? display : displayFn;
                function displayFn(obj) {
                    return obj[display];
                }
            }

            function getTemplates(templates, displayFn) {
                return {
                    empty: templates.empty && _.templatify(templates.empty),
                    header: templates.header && _.templatify(templates.header),
                    footer: templates.footer && _.templatify(templates.footer),
                    suggestion: templates.suggestion || suggestionTemplate
                };
                function suggestionTemplate(context) {
                    return "<p>" + displayFn(context) + "</p>";
                }
            }

            function isValidName(str) {
                return /^[_a-zA-Z0-9-]+$/.test(str);
            }
        }();
        var Dropdown = function () {
            "use strict";
            function Dropdown(o) {
                var that = this, onSuggestionClick, onSuggestionMouseEnter, onSuggestionMouseLeave;
                o = o || {};
                if (!o.menu) {
                    $.error("menu is required");
                }
                this.isOpen = false;
                this.isEmpty = true;
                this.datasets = _.map(o.datasets, initializeDataset);  // @ 将 datasets 中的每个 dataset 都转化成 Dataset 对象
                onSuggestionClick = _.bind(this._onSuggestionClick, this);
                onSuggestionMouseEnter = _.bind(this._onSuggestionMouseEnter, this);
                onSuggestionMouseLeave = _.bind(this._onSuggestionMouseLeave, this);
                this.$menu = $(o.menu).on("click.tt", ".tt-suggestion", onSuggestionClick).on("mouseenter.tt", ".tt-suggestion", onSuggestionMouseEnter).on("mouseleave.tt", ".tt-suggestion", onSuggestionMouseLeave);
                _.each(this.datasets, function (dataset) {
                    that.$menu.append(dataset.getRoot());  // @ 将每个 dataset 的容器元素逐个加入到下拉菜单
                    dataset.onSync("rendered", that._onRendered, that);  // @ 在 dataset 上注册 rendered 监听器（含义是 dataset 已渲染完毕），在 Dataset 对象执行 _render 或 clear 方法时会触发此事件
                });
            }

            _.mixin(Dropdown.prototype, EventEmitter, {
                _onSuggestionClick: function onSuggestionClick($e) {
                    this.trigger("suggestionClicked", $($e.currentTarget));
                },
                _onSuggestionMouseEnter: function onSuggestionMouseEnter($e) {
                    this._removeCursor();
                    this._setCursor($($e.currentTarget), true);
                },
                _onSuggestionMouseLeave: function onSuggestionMouseLeave() {
                    this._removeCursor();
                },
                _onRendered: function onRendered() {  // @ 当 dataset 渲染好时，开始渲染 dropdown
                    this.isEmpty = _.every(this.datasets, isDatasetEmpty);
                    this.isEmpty ? this._hide() : this.isOpen && this._show();  // @ 若所有 dataset 都为空则隐藏下拉菜单，若有 dataset 非空且下拉菜单没有打开则显示下拉菜单
                    this.trigger("datasetRendered");
                    function isDatasetEmpty(dataset) {
                        return dataset.isEmpty();
                    }
                },
                _hide: function () {
                    this.$menu.hide();
                },
                _show: function () {
                    this.$menu.css("display", "block");
                },
                _getSuggestions: function getSuggestions() {
                    return this.$menu.find(".tt-suggestion");
                },
                _getCursor: function getCursor() {
                    return this.$menu.find(".tt-cursor").first();
                },
                _setCursor: function setCursor($el, silent) {
                    $el.first().addClass("tt-cursor");
                    !silent && this.trigger("cursorMoved");
                },
                _removeCursor: function removeCursor() {
                    this._getCursor().removeClass("tt-cursor");
                },
                _moveCursor: function moveCursor(increment) {
                    var $suggestions, $oldCursor, newCursorIndex, $newCursor;
                    if (!this.isOpen) {
                        return;
                    }
                    $oldCursor = this._getCursor();
                    $suggestions = this._getSuggestions();
                    this._removeCursor();
                    newCursorIndex = $suggestions.index($oldCursor) + increment;
                    newCursorIndex = (newCursorIndex + 1) % ($suggestions.length + 1) - 1;
                    if (newCursorIndex === -1) {
                        this.trigger("cursorRemoved");
                        return;
                    } else if (newCursorIndex < -1) {
                        newCursorIndex = $suggestions.length - 1;
                    }
                    this._setCursor($newCursor = $suggestions.eq(newCursorIndex));
                    this._ensureVisible($newCursor);
                },
                _ensureVisible: function ensureVisible($el) {
                    var elTop, elBottom, menuScrollTop, menuHeight;
                    elTop = $el.position().top;
                    elBottom = elTop + $el.outerHeight(true);
                    menuScrollTop = this.$menu.scrollTop();
                    menuHeight = this.$menu.height() + parseInt(this.$menu.css("paddingTop"), 10) + parseInt(this.$menu.css("paddingBottom"), 10);
                    if (elTop < 0) {
                        this.$menu.scrollTop(menuScrollTop + elTop);
                    } else if (menuHeight < elBottom) {
                        this.$menu.scrollTop(menuScrollTop + (elBottom - menuHeight));
                    }
                },
                close: function close() {
                    if (this.isOpen) {
                        this.isOpen = false;
                        this._removeCursor();
                        this._hide();
                        this.trigger("closed");
                    }
                },
                open: function open() {
                    if (!this.isOpen) {
                        this.isOpen = true;
                        !this.isEmpty && this._show();
                        this.trigger("opened");
                    }
                },
                setLanguageDirection: function setLanguageDirection(dir) {
                    this.$menu.css(dir === "ltr" ? css.ltr : css.rtl);
                },
                moveCursorUp: function moveCursorUp() {
                    this._moveCursor(-1);
                },
                moveCursorDown: function moveCursorDown() {
                    this._moveCursor(+1);
                },
                getDatumForSuggestion: function getDatumForSuggestion($el) {
                    var datum = null;
                    if ($el.length) {
                        datum = {
                            raw: Dataset.extractDatum($el),
                            value: Dataset.extractValue($el),
                            datasetName: Dataset.extractDatasetName($el)
                        };
                    }
                    return datum;
                },
                getDatumForCursor: function getDatumForCursor() {
                    return this.getDatumForSuggestion(this._getCursor().first());
                },
                getDatumForTopSuggestion: function getDatumForTopSuggestion() {
                    return this.getDatumForSuggestion(this._getSuggestions().first());
                },
                update: function update(query) {
                    _.each(this.datasets, updateDataset);
                    function updateDataset(dataset) {
                        dataset.update(query);
                    }
                },
                empty: function empty() {
                    _.each(this.datasets, clearDataset);
                    this.isEmpty = true;
                    function clearDataset(dataset) {
                        dataset.clear();
                    }
                },
                isVisible: function isVisible() {
                    return this.isOpen && !this.isEmpty;
                },
                destroy: function destroy() {
                    this.$menu.off(".tt");
                    this.$menu = null;
                    _.each(this.datasets, destroyDataset);
                    function destroyDataset(dataset) {
                        dataset.destroy();
                    }
                }
            });
            return Dropdown;
            function initializeDataset(oDataset) {
                return new Dataset(oDataset);
            }
        }();
        var Typeahead = function () {
            "use strict";
            var attrsKey = "ttAttrs";

            function Typeahead(o) {
                var $menu, $input, $hint;
                o = o || {};
                if (!o.input) {
                    $.error("missing input");
                }
                this.isActivated = false;
                this.autoselect = !!o.autoselect;
                this.minLength = _.isNumber(o.minLength) ? o.minLength : 1;
                this.$node = buildDom(o.input, o.withHint);  // @ $node 是包裹了输入框的 wrapper
                $menu = this.$node.find(".tt-dropdown-menu");
                $input = this.$node.find(".tt-input");
                $hint = this.$node.find(".tt-hint");
                $input.on("blur.tt", function ($e) {
                    var active, isActive, hasActive;
                    active = document.activeElement;
                    isActive = $menu.is(active);
                    hasActive = $menu.has(active).length > 0;
                    if (_.isMsie() && (isActive || hasActive)) {
                        $e.preventDefault();
                        $e.stopImmediatePropagation();
                        _.defer(function () {
                            $input.focus();  // @ TODO 针对 IE 写的这个 focus() 是何用意？
                        });
                    }
                });
                $menu.on("mousedown.tt", function ($e) {
                    $e.preventDefault();
                });
                this.eventBus = o.eventBus || new EventBus({
                    el: $input
                });
                this.dropdown = new Dropdown({
                    menu: $menu,
                    datasets: o.datasets
                }).onSync("suggestionClicked", this._onSuggestionClicked, this)
                        .onSync("cursorMoved", this._onCursorMoved, this)
                        .onSync("cursorRemoved", this._onCursorRemoved, this)
                        .onSync("opened", this._onOpened, this)
                        .onSync("closed", this._onClosed, this)
                        .onAsync("datasetRendered", this._onDatasetRendered, this);
                this.input = new Input({
                    input: $input,
                    hint: $hint
                }).onSync("focused", this._onFocused, this)
                        .onSync("blurred", this._onBlurred, this)
                        .onSync("enterKeyed", this._onEnterKeyed, this)
                        .onSync("tabKeyed", this._onTabKeyed, this)
                        .onSync("escKeyed", this._onEscKeyed, this)
                        .onSync("upKeyed", this._onUpKeyed, this)
                        .onSync("downKeyed", this._onDownKeyed, this)
                        .onSync("leftKeyed", this._onLeftKeyed, this)
                        .onSync("rightKeyed", this._onRightKeyed, this)
                        .onSync("queryChanged", this._onQueryChanged, this)
                        .onSync("whitespaceChanged", this._onWhitespaceChanged, this);
                this._setLanguageDirection();
            }

            _.mixin(Typeahead.prototype, {
                _onSuggestionClicked: function onSuggestionClicked(type, $el) {
                    var datum;
                    if (datum = this.dropdown.getDatumForSuggestion($el)) {
                        this._select(datum);
                    }
                },
                _onCursorMoved: function onCursorMoved() {
                    var datum = this.dropdown.getDatumForCursor();
                    this.input.setInputValue(datum.value, true);
                    this.eventBus.trigger("cursorchanged", datum.raw, datum.datasetName);
                },
                _onCursorRemoved: function onCursorRemoved() {
                    this.input.resetInputValue();

                    // 为了在避免在 $input 没有文字时 $hint 遮盖 placeholder
                    if (this.input.$input.val().length !== 0) {
                        this._updateHint();
                    }

                    //this._updateHint();
                },
                _onDatasetRendered: function onDatasetRendered() {

                    // 为了在避免在 $input 没有文字时 $hint 遮盖 placeholder
                    if (this.input.$input.val().length !== 0) {
                        this._updateHint();
                    }

                    //this._updateHint();
                },
                _onOpened: function onOpened() {

                    // 为了在避免在 $input 没有文字时 $hint 遮盖 placeholder
                    if (this.input.$input.val().length !== 0) {
                        this._updateHint();
                    }

                    //this._updateHint();
                    this.eventBus.trigger("opened");
                },
                _onClosed: function onClosed() {
                    this.input.clearHint();
                    this.eventBus.trigger("closed");
                },
                _onFocused: function onFocused() {
                    this.isActivated = true;

                    ////为使点击空的输入框也能获得下拉菜单推荐，做的改动
                    //if (this.input.$input.val().length === 0) {
                    //    this.dropdown.update('');  // 以前之所以点击空输入框没有下拉菜单，是因为 focus 事件处理函数没有 update dropdown（也就是没有 update dataset）
                    //}

                    this.dropdown.open();
                },
                _onBlurred: function onBlurred() {
                    this.isActivated = false;
                    this.dropdown.empty();
                    this.dropdown.close();
                },
                _onEnterKeyed: function onEnterKeyed(type, $e) {
                    var cursorDatum, topSuggestionDatum;
                    cursorDatum = this.dropdown.getDatumForCursor();
                    topSuggestionDatum = this.dropdown.getDatumForTopSuggestion();
                    if (cursorDatum) {
                        this._select(cursorDatum);
                        $e.preventDefault();
                    } else if (this.autoselect && topSuggestionDatum) {
                        this._select(topSuggestionDatum);
                        $e.preventDefault();
                    }
                },
                _onTabKeyed: function onTabKeyed(type, $e) {
                    var datum;
                    if (datum = this.dropdown.getDatumForCursor()) {
                        this._select(datum);
                        $e.preventDefault();
                    } else {
                        this._autocomplete(true);
                    }
                },
                _onEscKeyed: function onEscKeyed() {
                    this.dropdown.close();
                    this.input.resetInputValue();
                },
                _onUpKeyed: function onUpKeyed() {
                    var query = this.input.getQuery();
                    this.dropdown.isEmpty && query.length >= this.minLength ? this.dropdown.update(query) : this.dropdown.moveCursorUp();
                    this.dropdown.open();
                },
                _onDownKeyed: function onDownKeyed() {
                    var query = this.input.getQuery();
                    this.dropdown.isEmpty && query.length >= this.minLength ? this.dropdown.update(query) : this.dropdown.moveCursorDown();
                    this.dropdown.open();
                },
                _onLeftKeyed: function onLeftKeyed() {
                    this.dir === "rtl" && this._autocomplete();
                },
                _onRightKeyed: function onRightKeyed() {
                    this.dir === "ltr" && this._autocomplete();
                },
                _onQueryChanged: function onQueryChanged(e, query) {
                    this.input.clearHintIfInvalid();
                    query.length >= this.minLength ? this.dropdown.update(query) : this.dropdown.empty();
                    this.dropdown.open();
                    this._setLanguageDirection();
                },
                _onWhitespaceChanged: function onWhitespaceChanged() {
                    this._updateHint();
                    this.dropdown.open();
                },
                _setLanguageDirection: function setLanguageDirection() {
                    var dir;
                    if (this.dir !== (dir = this.input.getLanguageDirection())) {
                        this.dir = dir;
                        this.$node.css("direction", dir);
                        this.dropdown.setLanguageDirection(dir);
                    }
                },
                _updateHint: function updateHint() {
                    var datum, val, query, escapedQuery, frontMatchRegEx, match;
                    datum = this.dropdown.getDatumForTopSuggestion();
                    if (datum && this.dropdown.isVisible() && !this.input.hasOverflow()) {
                        val = this.input.getInputValue();
                        query = Input.normalizeQuery(val);
                        escapedQuery = _.escapeRegExChars(query);
                        frontMatchRegEx = new RegExp("^(?:" + escapedQuery + ")(.+$)", "i");
                        match = frontMatchRegEx.exec(datum.value);
                        match ? this.input.setHint(val + match[1]) : this.input.clearHint();
                    } else {
                        this.input.clearHint();
                    }
                },
                _autocomplete: function autocomplete(laxCursor) {
                    var hint, query, isCursorAtEnd, datum;
                    hint = this.input.getHint();
                    query = this.input.getQuery();
                    isCursorAtEnd = laxCursor || this.input.isCursorAtEnd();
                    if (hint && query !== hint && isCursorAtEnd) {
                        datum = this.dropdown.getDatumForTopSuggestion();
                        datum && this.input.setInputValue(datum.value);
                        this.eventBus.trigger("autocompleted", datum.raw, datum.datasetName);
                    }
                },
                _select: function select(datum) {
                    this.input.setQuery(datum.value);
                    this.input.setInputValue(datum.value, true);
                    this._setLanguageDirection();
                    this.eventBus.trigger("selected", datum.raw, datum.datasetName);
                    this.dropdown.close();
                    _.defer(_.bind(this.dropdown.empty, this.dropdown));
                },
                open: function open() {
                    this.dropdown.open();
                },
                close: function close() {
                    this.dropdown.close();
                },
                setVal: function setVal(val) {
                    val = _.toStr(val);
                    if (this.isActivated) {
                        this.input.setInputValue(val);
                    } else {
                        this.input.setQuery(val);
                        this.input.setInputValue(val, true);
                    }
                    this._setLanguageDirection();
                },
                getVal: function getVal() {
                    return this.input.getQuery();
                },
                destroy: function destroy() {
                    this.input.destroy();
                    this.dropdown.destroy();
                    destroyDomStructure(this.$node);
                    this.$node = null;
                }
            });
            return Typeahead;
            function buildDom(input, withHint) {  // @ 从 Dom 结构上，将一个输入框改造为 typeahead 输入框
                var $input, $wrapper, $dropdown, $hint;
                $input = $(input);  // @ 注意，若 input 本身就是 jQuery 对象(A)（根据代码，有很大可能是），则 jQuery 选择器会生成一个新的 jQuery 对象(B)。A 和 B 是不同对象，但 AB 中携带的原生 DOM 对象引用是相同的
                $wrapper = $(html.wrapper).css(css.wrapper);  // @ 外壳
                $dropdown = $(html.dropdown).css(css.dropdown);  // @ 下拉菜单
                $hint = $input.clone().css(css.hint).css(getBackgroundStyles($input));  // @ 输入框层之下的提示层
                $hint.val("").removeData().addClass("tt-hint").removeAttr("id name placeholder required").prop("readonly", true).attr({  // @ 加入 readonly 属性，这样就不会干扰上层 $input 的操作了
                    autocomplete: "off",
                    spellcheck: "false",
                    tabindex: -1
                });
                $input.data(attrsKey, {
                    dir: $input.attr("dir"),
                    autocomplete: $input.attr("autocomplete"),
                    spellcheck: $input.attr("spellcheck"),
                    style: $input.attr("style")
                });
                $input.addClass("tt-input").attr({
                    autocomplete: "off",
                    spellcheck: false
                }).css(withHint ? css.input : css.inputWithNoHint);
                try {
                    !$input.attr("dir") && $input.attr("dir", "auto");
                } catch (e) {
                }
                return $input.wrap($wrapper).parent().prepend(withHint ? $hint : null).append($dropdown);  // @ 将 $input 外面包裹上 $wrapper，再将 $hint 和 $dropdown 分别置于 $input 前后
            }

            function getBackgroundStyles($el) {
                return {
                    backgroundAttachment: $el.css("background-attachment"),
                    backgroundClip: $el.css("background-clip"),
                    backgroundColor: $el.css("background-color"),
                    backgroundImage: $el.css("background-image"),
                    backgroundOrigin: $el.css("background-origin"),
                    backgroundPosition: $el.css("background-position"),
                    backgroundRepeat: $el.css("background-repeat"),
                    backgroundSize: $el.css("background-size")
                };
            }

            function destroyDomStructure($node) {
                var $input = $node.find(".tt-input");
                _.each($input.data(attrsKey), function (val, key) {
                    _.isUndefined(val) ? $input.removeAttr(key) : $input.attr(key, val);
                });
                $input.detach().removeData(attrsKey).removeClass("tt-input").insertAfter($node);
                $node.remove();
            }
        }();
        (function () {
            "use strict";
            var old, typeaheadKey, methods;
            old = $.fn.typeahead;
            typeaheadKey = "ttTypeahead";
            methods = {
                initialize: function initialize(o, datasets) {  // @ 初始化方法，一切开始的地方
                    datasets = _.isArray(datasets) ? datasets : [].slice.call(arguments, 1);  // @ 如果 datasets 不是数组，则把 initialize() 第一个参数之后的所有参数组合成一个数组
                    o = o || {};
                    return this.each(attach);  // @ this 指向一个 jQuery 对象。对于该对象内的每个元素，都执行 attach() 函数
                    function attach() {
                        var $input = $(this), eventBus, typeahead;
                        _.each(datasets, function (d) {
                            d.highlight = !!o.highlight;  // @ 将 option 中的 highlight 选项赋给每个 dataset
                        });
                        typeahead = new Typeahead({
                            input: $input,
                            eventBus: eventBus = new EventBus({
                                el: $input
                            }),
                            withHint: _.isUndefined(o.hint) ? true : !!o.hint,  // @ o.hint 缺省值为 true
                            minLength: o.minLength,
                            autoselect: o.autoselect,
                            datasets: datasets
                        });
                        $input.data(typeaheadKey, typeahead);  // @ 将 Typeahead 对象存入输入框元素中
                    }
                },
                open: function open() {
                    return this.each(openTypeahead);
                    function openTypeahead() {
                        var $input = $(this), typeahead;
                        if (typeahead = $input.data(typeaheadKey)) {
                            typeahead.open();
                        }
                    }
                },
                close: function close() {
                    return this.each(closeTypeahead);
                    function closeTypeahead() {
                        var $input = $(this), typeahead;
                        if (typeahead = $input.data(typeaheadKey)) {
                            typeahead.close();
                        }
                    }
                },
                val: function val(newVal) {
                    return !arguments.length ? getVal(this.first()) : this.each(setVal);
                    function setVal() {
                        var $input = $(this), typeahead;
                        if (typeahead = $input.data(typeaheadKey)) {
                            typeahead.setVal(newVal);
                        }
                    }

                    function getVal($input) {
                        var typeahead, query;
                        if (typeahead = $input.data(typeaheadKey)) {
                            query = typeahead.getVal();
                        }
                        return query;
                    }
                },
                destroy: function destroy() {
                    return this.each(unattach);
                    function unattach() {
                        var $input = $(this), typeahead;
                        if (typeahead = $input.data(typeaheadKey)) {
                            typeahead.destroy();
                            $input.removeData(typeaheadKey);
                        }
                    }
                }
            };
            $.fn.typeahead = function (method) {
                var tts;
                if (methods[method] && method !== "initialize") {
                    tts = this.filter(function () {  // @ 选出当前 jQuery 对象中带有 Typeahead 对象的元素组成的集合
                        return !!$(this).data(typeaheadKey);
                    });
                    return methods[method].apply(tts, [].slice.call(arguments, 1)); // @ 将 method 以后的参数（可能为空）作为 methods[method] 方法的参数（其实只有 val() 方法）期待参数
                } else {
                    return methods.initialize.apply(this, arguments);  // @ 若没有传入方法名称，或传入的是 "initialize" ，则进行初始化
                }
            };
            $.fn.typeahead.noConflict = function noConflict() {
                $.fn.typeahead = old;
                return this;
            };
        })();
    };

});