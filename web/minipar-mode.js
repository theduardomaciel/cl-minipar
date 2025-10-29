// Modo de Syntax Highlighting para MiniPar no CodeMirror
(function (mod) {
    if (typeof exports == "object" && typeof module == "object") // CommonJS
        mod(require("../../lib/codemirror"));
    else if (typeof define == "function" && define.amd) // AMD
        define(["../../lib/codemirror"], mod);
    else // Plain browser env
        mod(CodeMirror);
})(function (CodeMirror) {
    "use strict";

    CodeMirror.defineMode("minipar", function (config, parserConfig) {
        var indentUnit = config.indentUnit;

        // Palavras-chave da linguagem MiniPar
        var keywords = {
            "class": true, "extends": true, "new": true, "this": true, "super": true,
            "func": true, "var": true, "if": true, "else": true, "while": true,
            "do": true, "for": true, "return": true, "break": true, "continue": true,
            "seq": true, "par": true, "c_channel": true, "s_channel": true,
            "in": true, "print": true, "println": true, "input": true
        };

        // Tipos
        var types = {
            "number": true, "string": true, "bool": true, "void": true,
            "list": true, "dict": true
        };

        // Literais
        var atoms = {
            "true": true, "false": true, "null": true
        };

        // Operadores
        var isOperatorChar = /[+\-*\/%&|^~<>!=]/;

        function tokenBase(stream, state) {
            var ch = stream.next();

            // Comentários de linha
            if (ch == "#") {
                stream.skipToEnd();
                return "comment";
            }

            // Comentários de bloco
            if (ch == "/" && stream.eat("*")) {
                state.tokenize = tokenComment;
                return tokenComment(stream, state);
            }

            // Strings
            if (ch == '"') {
                state.tokenize = tokenString(ch);
                return state.tokenize(stream, state);
            }

            // Números
            if (/\d/.test(ch)) {
                stream.match(/^\d*(?:\.\d+)?/);
                return "number";
            }

            // Operadores e pontuação
            if (isOperatorChar.test(ch)) {
                stream.eatWhile(isOperatorChar);
                return "operator";
            }

            // Palavras-chave, tipos e identificadores
            if (/[a-zA-Z_]/.test(ch)) {
                stream.eatWhile(/[\w]/);
                var word = stream.current();

                if (keywords.hasOwnProperty(word))
                    return "keyword";
                if (types.hasOwnProperty(word))
                    return "variable-2";
                if (atoms.hasOwnProperty(word))
                    return "atom";

                return "variable";
            }

            return null;
        }

        function tokenString(quote) {
            return function (stream, state) {
                var escaped = false, next, end = false;
                while ((next = stream.next()) != null) {
                    if (next == quote && !escaped) {
                        end = true;
                        break;
                    }
                    escaped = !escaped && next == "\\";
                }
                if (end || !escaped)
                    state.tokenize = tokenBase;
                return "string";
            };
        }

        function tokenComment(stream, state) {
            var maybeEnd = false, ch;
            while (ch = stream.next()) {
                if (ch == "/" && maybeEnd) {
                    state.tokenize = tokenBase;
                    break;
                }
                maybeEnd = (ch == "*");
            }
            return "comment";
        }

        return {
            startState: function () {
                return {
                    tokenize: tokenBase,
                    indented: 0,
                    context: null
                };
            },

            token: function (stream, state) {
                if (stream.sol()) {
                    if (state.context && state.context.align == null)
                        state.context.align = false;
                    state.indented = stream.indentation();
                }
                if (stream.eatSpace()) return null;

                var style = state.tokenize(stream, state);
                return style;
            },

            indent: function (state, textAfter) {
                if (state.tokenize != tokenBase) return CodeMirror.Pass;
                var ctx = state.context;
                if (!ctx) return 0;
                var closing = textAfter.charAt(0) == ctx.type;
                if (ctx.align) return ctx.column + (closing ? 0 : 1);
                else return ctx.indent + (closing ? 0 : indentUnit);
            },

            electricChars: "{}",
            lineComment: "#",
            blockCommentStart: "/*",
            blockCommentEnd: "*/",
            fold: "brace"
        };
    });

    CodeMirror.defineMIME("text/x-minipar", "minipar");
});
