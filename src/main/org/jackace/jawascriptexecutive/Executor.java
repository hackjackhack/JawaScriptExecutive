/*
Copyright (c) 2016, Chi-Wei(Jack) Wang
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the intowow nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL Chi-Wei(Jack) Wang BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.jackace.jawascriptexecutive;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

/**
 * Created by Chi-Wei(Jack) Wang on 2015/12/25.
 */
public class Executor {
    private static final int SCRIPT_BODY = 0;
    private static final int FUNCTION_DECLARATION = 1;
    private static final int BLOCK_STATEMENT = 2;
    private static final int EMPTY_STATEMENT = 3;
    private static final int SEQUENCE_EXPRESSION = 4;

    private static final int ASSIGNMENT_EXPRESSION = 5;
    private static final int CONDITIONAL_EXPRESSION = 6;
    private static final int LOGICAL_OR_EXPRESSION = 7;
    private static final int LOGICAL_AND_EXPRESSION = 8;
    private static final int INCLUSIVE_OR_EXPRESSION = 9;

    private static final int EXCLUSIVE_OR_EXPRESSION = 10;
    private static final int AND_EXPRESSION = 11;
    private static final int EQUALITY_EXPRESSION = 12;
    private static final int RELATIONAL_EXPRESSION = 13;
    private static final int IN_EXPRESSION = 14;

    private static final int SHIFT_EXPRESSION = 15;
    private static final int ADDITIVE_EXPRESSION = 16;
    private static final int MULTIPLICATIVE_EXPRESSION = 17;
    private static final int UNARY_EXPRESSION = 18;
    private static final int POSTFIX_EXPRESSION = 19;

    private static final int STATIC_MEMBER_EXPRESSION = 20;
    private static final int CALL_EXPRESSION = 21;
    private static final int COMPUTED_MEMBER_EXPRESSION = 22;
    private static final int NEW_EXPRESSION = 23;
    private static final int IDENTIFIER = 24;

    private static final int LITERAL = 25;
    private static final int ARGUMENTS = 26;
    private static final int ARRAY_EXPRESSION = 27;
    private static final int OBJECT_EXPRESSION = 28;
    private static final int BREAK_STATEMENT = 29;

    private static final int CONTINUE_STATEMENT = 30;
    private static final int DO_WHILE_STATEMENT = 31;
    private static final int ITERATOR_DECLARATION = 32;
    private static final int FOR_STATEMENT = 33;
    private static final int VARIABLE_DECLARATION = 34;

    private static final int IF_STATEMENT = 35;
    private static final int RETURN_STATEMENT =36;
    private static final int VAR_STATEMENT = 37;
    private static final int WHILE_STATEMENT = 38;
    private static final int OBJECT_PROPERTY = 39;

    ////////////////////////////////////////////////////////
    private static final int PR_statements = 0;
    private static final int PR_valueType = 1;
    private static final int PR_arguments = 2;
    private static final int PR_id = 3;
    private static final int PR_key = 4;

    private static final int PR_expr = 5;
    private static final int PR_properties = 6;
    private static final int PR_elements = 7;
    private static final int PR_literal = 8;
    private static final int PR_constructor = 9;

    private static final int PR_object = 10;
    private static final int PR_property = 11;
    private static final int PR_function = 12;
    private static final int PR_subExpression = 13;
    private static final int PR_op = 14;

    private static final int PR_ops = 15;
    private static final int PR_subExpressions = 16;
    private static final int PR_condition = 17;
    private static final int PR_onTrue = 18;
    private static final int PR_onFalse = 19;

    private static final int PR_left = 20;
    private static final int PR_right = 21;
    private static final int PR_expressions = 22;
    private static final int PR_params = 23;
    private static final int PR_body = 24;

    private static final int PR_test = 25;
    private static final int PR_varName = 26;
    private static final int PR_initialization = 27;
    private static final int PR_iterable = 28;
    private static final int PR_iterator = 29;

    private static final int PR_init = 30;
    private static final int PR_update = 31;
    private static final int PR_argument = 32;
    private static final int PR_declarations = 33;

    // JSON functions
    private JSONObject getObj(JSONObject ast, int t) throws JSONException {
        return ast.getJSONObject(Integer.toString(t));
    }
    private JSONArray getArray(JSONObject ast, int t) throws JSONException {
        return ast.getJSONArray(Integer.toString(t));
    }

    private String getString(JSONObject ast, int t) throws JSONException {
        return ast.getString(Integer.toString(t));
    }

    ////////////////////////////////////////////////////
    private static final double QUANTUM = 0.0000000000000001;

    private class JawascriptRuntimeException extends Exception {
        JawascriptRuntimeException (String msg) {
            super(msg);
        }
    }

    ////////////////////////////////////////////////////
    private int toInteger(JawaObjectRef o) throws JawascriptRuntimeException {
        if (o.object instanceof Double) {
            int magnitude = (int)(long) Math.floor(Math.abs((Double) o.object));
            int sign = ((Double) o.object) > 0 ? 1 : -1;
            return magnitude * sign;
        }
        throw new JawascriptRuntimeException("Not yet implemented conversion.");
    }
    ////////////////////////////////////////////////////
    private class JawaObjectRef {
        Object object;
        JawaObjectRef self;

        JawaObjectRef() {object = null;}
        JawaObjectRef(double value) {object = value;}
        JawaObjectRef(String value) {object = new StringBuilder(value);}
        JawaObjectRef(boolean value) {object = value;}
        JawaObjectRef(JawaArray value) {object = value;}
        JawaObjectRef(JawaFunc value) {object = value; this.self = null;}
        JawaObjectRef(JawaFunc value, JawaObjectRef self) {object = value; this.self = self;}
        JawaObjectRef(JawaObject value) {object = value;}

        public String toString() {
            if (this.object instanceof Double) {
                double value = (Double)this.object;
                if (Math.abs(Math.round(value) - value) < QUANTUM) {
                    return Long.toString(Math.round(value));
                } else {
                    DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
                    df.setMaximumFractionDigits(340);
                    return df.format(value);
                }
            } else if (this.object instanceof Boolean) {
                return Boolean.toString((Boolean)this.object);
            }
            if (object != null)
                return object.toString();

            return null;
        }

        Object transfer() {
            if (object instanceof Double)
                return new Double(((Double)object).doubleValue());
            else if(object instanceof StringBuilder)
                return new StringBuilder(object.toString());
            else if(object instanceof Boolean)
                return new Boolean(((Boolean)object).booleanValue());
            else
                return object;
        }
    }

    ////////////////////////////////////////////////////
    private void registerBuiltinInPrototype(HashMap<String, JawaFunc> prototype,
                                            String funcName,
                                            List<String> params,
                                            boolean isProperty) {
        JawaFunc f = new JawaFunc(funcName, params, true, isProperty, null);
        f.switchId = prototype.size();
        prototype.put(funcName, f);
    }

    private void registerBuiltinFunc(HashMap<String, JawaFunc> prototype,
                                     String funcName,
                                     List<String> params) {
        registerBuiltinInPrototype(prototype, funcName, params, false);
    }

    private void registerBuiltinProp(HashMap<String, JawaFunc> prototype,
                                     String PropName) {
        registerBuiltinInPrototype(prototype, PropName, null, true);
    }

    ////////////////////////////////////////////////////
    private HashMap<String, JawaFunc> objectPrototype = new HashMap<String, JawaFunc>();
    private class JawaObject {
        HashMap<String, JawaObjectRef> properties = new HashMap<String, JawaObjectRef>();
        HashMap<String, JawaFunc> prototype;

        JawaObject() { this.prototype = objectPrototype; }

        void setProp(String key, JawaObjectRef value) {
            properties.put(key, value);
        }

        JawaObjectRef getProp(String key) {
            if (properties.get(key) != null)
                return properties.get(key);
            if (objectPrototype.get(key) != null)
                return new JawaObjectRef(objectPrototype.get(key), new JawaObjectRef(this));
            return null;
        }

        int getBuiltinID(String funcName) throws JawascriptRuntimeException {
            if (prototype.get(funcName) == null)
                throw new JawascriptRuntimeException("The object has no method " + funcName + "()");
            return prototype.get(funcName).switchId;
        }

        JawaObjectRef invokeBuiltin(String funcName) throws JawascriptRuntimeException {
            int id = getBuiltinID(funcName);
            switch (id) {
                // toJSON()
                case 0: {
                    return new JawaObjectRef(this.toJSON(null).toString());
                }
                default:
                    throw new JawascriptRuntimeException(funcName + "() not yet implemented.");
            }
        }

        public String toString() {
            String ret = "{";
            boolean first = true;
            for (String key : this.properties.keySet()) {
                if (!first)
                    ret += ",";
                first = false;
                ret += key + ":";
                JawaObjectRef value = this.properties.get(key);
                if (value.object instanceof StringBuilder)
                    ret += "'" + value.toString() + "'";
                else
                    ret += value.toString();
            }
            ret += "}";
            return ret;
        }

        public StringBuilder toJSON(StringBuilder ret) {
            if (ret == null)
                ret = new StringBuilder();
            ret.append("{");
            boolean first = true;
            for (String key : this.properties.keySet()) {
                if (!first)
                    ret.append(",");
                first = false;
                ret.append("\"").append(key).append("\":");
                JawaObjectRef value = this.properties.get(key);
                if (value.object instanceof StringBuilder)
                    ret.append("\"").append(value.toString().replace("\"", "\\\"")).append("\"");
                else if (value.object instanceof JawaObject)
                    ((JawaObject)(value.object)).toJSON(ret);
                else
                    ret.append(value.toString());
            }
            ret.append("}");
            return ret;
        }
    }

    ////////////////////////////////////////////////////
    private HashMap<String, JawaFunc> arrayPrototype = new HashMap<String, JawaFunc>();
    private class JawaComparator implements Comparator<JawaObjectRef> {
        private JawaFunc comparator = null;
        public JawaComparator(JawaObjectRef comparator) {
            if (comparator != null)
                this.comparator = (JawaFunc)comparator.object;
        }
        public int compare(JawaObjectRef lhs, JawaObjectRef rhs) {
            if (comparator != null) {
                HashMap<String, JawaObjectRef> scope = new HashMap<String, JawaObjectRef>();
                scope.put(comparator.params.get(0), lhs);
                scope.put(comparator.params.get(1), rhs);

                LinkedList<HashMap<String, JawaObjectRef>> activation = new LinkedList<HashMap<String, JawaObjectRef>>();
                activation.addLast(scope);
                boolean oldIsFromCallExpression = isFromCallExpression;
                isFromCallExpression = true;
                activations.addLast(activation);
                currentActivation = activation;
                int compResult = 0;
                try {
                    JawaObjectRef ret;
                    ret = comparator.apply();
                    double v = (Double)ret.object;
                    compResult = v < 0 ? -1 : (v == 0 ? 0 : 1);
                } catch (Exception e) {}
                activations.removeLast();
                currentActivation = activations.getLast();
                isFromCallExpression = oldIsFromCallExpression;
                return compResult;
            } else {
                return lhs.object.toString().compareTo(rhs.object.toString());
            }
        }
    }
    private class JawaArray extends JawaObject {
        ArrayList<JawaObjectRef> elements = new ArrayList<JawaObjectRef>();

        JawaArray() { this.prototype = arrayPrototype; }

        public String toString() {
            boolean first = true;
            String ret = "[";
            for (JawaObjectRef obj : this.elements) {
                if (!first)
                    ret += ",";
                first = false;
                ret += obj.toString();
            }
            ret += "]";
            return ret;
        }

        public StringBuilder toJSON(StringBuilder ret) {
            if (ret == null)
                ret = new StringBuilder();
            boolean first = true;
            ret.append("[");
            for (JawaObjectRef obj : this.elements) {
                if (!first)
                    ret.append(",");
                first = false;
                if (obj.object instanceof StringBuilder)
                    ret.append('"').append(obj.toString().replace("\"", "\\\"")).append('"');
                else if (obj.object instanceof JawaObject)
                    ((JawaObject)obj.object).toJSON(ret);
                else
                    ret.append(obj.toString());
            }
            ret.append("]");
            return ret;
        }

        void append(JawaObjectRef element) {
            elements.add(element);
        }

        JawaObjectRef at(int index) throws JawascriptRuntimeException {
            if (index < 0 || index >= elements.size())
                throw new JawascriptRuntimeException("Out of bound.");
            return elements.get(index);
        }

        JawaObjectRef getProp(String key) {
            if (properties.get(key) != null)
                return properties.get(key);
            if (arrayPrototype.get(key) != null)
                return new JawaObjectRef(arrayPrototype.get(key), new JawaObjectRef(this));
            return super.getProp(key);
        }

        JawaObjectRef invokeBuiltin(String funcName) throws JawascriptRuntimeException {
            int id = getBuiltinID(funcName);
            switch (id) {

                // Array.length
                case 0: {
                    return new JawaObjectRef(this.elements.size());
                }
                // Array.slice(start, end)
                case 1: {
                    JawaObjectRef start = currentActivation.getLast().get("start");
                    if (!(start.object instanceof Double))
                        throw new JawascriptRuntimeException("start of slice() must be a number");
                    int startInt = ((Double) start.object).intValue();
                    if (startInt < 0 || startInt >= elements.size())
                        throw new JawascriptRuntimeException("begin out of bound.");

                    int endInt = elements.size();
                    JawaObjectRef end = currentActivation.getLast().get("end");
                    if (end != null) {
                        if (!(end.object instanceof Double))
                            throw new JawascriptRuntimeException("end of slice() must be a number");
                        endInt = ((Double) end.object).intValue();
                        if (endInt < 0 || endInt >= elements.size())
                            throw new JawascriptRuntimeException("begin out of bound.");
                    }

                    JawaArray slice = new JawaArray();
                    for (int i = startInt; i < endInt; i++)
                        slice.append(elements.get(i));
                    return new JawaObjectRef(slice);
                }
                // Array.join(sep)
                case 2: {
                    JawaObjectRef sep = currentActivation.getLast().get("sep");
                    if (!(sep.object instanceof StringBuilder))
                        throw new JawascriptRuntimeException("separator must be a string");
                    String sepStr = sep.toString();
                    String ret = "";
                    boolean first = true;
                    for (JawaObjectRef o : elements) {
                        if (!first)
                            ret += sepStr;
                        first = false;
                        ret += o.toString();
                    }
                    return new JawaObjectRef(ret);
                }

                // Array.pop()
                case 3: {
                    if (elements.size() <= 0)
                        return null;
                    return elements.remove(elements.size() - 1);
                }
                // Array.push(item)
                case 4: {
                    JawaObjectRef item = currentActivation.getLast().get("item");
                    elements.add(item);
                    return new JawaObjectRef(elements.size());
                }
                // Array.reverse()
                case 5: {
                    for (int i = 0 ; i < elements.size() / 2 ; i++) {
                        JawaObjectRef t = elements.get(i);
                        elements.set(i, elements.get(elements.size() - 1 - i));
                        elements.set(elements.size() - 1 - i, t);
                    }
                    return new JawaObjectRef(this);
                }
                // Array.shift()
                case 6: {
                    if (elements.size() <= 0)
                        return null;
                    return elements.remove(0);
                }
                // Array.sort(compareFunction)
                case 7: {
                    JawaObjectRef compareFunction = currentActivation.getLast().get("compareFunction");
                    Collections.sort(this.elements, new JawaComparator(compareFunction));
                    return new JawaObjectRef(this);
                }
                // Array.unshift()
                case 8: {
                    JawaObjectRef item = currentActivation.getLast().get("item");
                    elements.add(0, item);
                    return new JawaObjectRef(elements.size());
                }

                default:
                    throw new JawascriptRuntimeException(funcName + "() not yet implemented.");
            }
        }
    }

    ////////////////////////////////////////////////////
    private class JawaFunc extends JawaObject {
        String name;
        JSONObject body;
        List<String> params;
        boolean isBuiltIn = false;
        boolean isPropertyWrapper = false;
        int switchId;

        JawaFunc (String name, List<String> params, boolean isBuiltIn,
                  boolean isPropertyWrapper, JSONObject body) {
            this.name = name;
            this.params = params;
            this.isBuiltIn = isBuiltIn;
            this.isPropertyWrapper = isPropertyWrapper;
            this.body = body;
        }

        JawaObjectRef apply(JawaObjectRef self) throws JSONException, JawascriptRuntimeException {
            if (!isBuiltIn)
                throw new JawascriptRuntimeException("apply(self) should always be native.");
            if (self.object instanceof StringBuilder) {
                return dispatchStringBuiltin((StringBuilder) self.object, this.name);
            } else if (self.object instanceof JawaObject) {
                return ((JawaObject)self.object).invokeBuiltin(this.name);
            } else
                throw new JawascriptRuntimeException("Not yet implemented.");
        }

        JawaObjectRef apply() throws JSONException, JawascriptRuntimeException {
            if (!isBuiltIn) {
                evaluate(body);
                return currentActivation.getFirst().get("return");
            } else
                return dispatchBuiltin(this.name);
        }

        public String toString() {
            return "function";
        }
    }

    ////////////////////////////////////////////////////
    private HashMap<String, JawaFunc> stringPrototype = new HashMap<String, JawaFunc>();
    private JawaObjectRef dispatchStringBuiltin(StringBuilder str, String funcName) throws JawascriptRuntimeException {
        if (stringPrototype.get(funcName) == null)
            throw new JawascriptRuntimeException("string has no method " + funcName + "().");
        int funcId = stringPrototype.get(funcName).switchId;
        // Assumption: The following sequence must be as same as the registering sequence.

        switch (funcId) {
            // String.split(delim)
            case 0: {
                JawaArray result = new JawaArray();
                String delim = currentActivation.getLast().get("delim").toString();
                if (delim.isEmpty()) {
                    for (int i = 0; i < str.length(); i++)
                        result.append(new JawaObjectRef(str.substring(i, i + 1)));
                } else {
                    String[] splitted = str.toString().split(currentActivation.getLast().get("delim").toString());
                    for (String e : splitted)
                        result.append(new JawaObjectRef(e));
                }
                return new JawaObjectRef(result);
            }
            // String.length
            case 1: {
                return new JawaObjectRef(str.length());
            }
            // String.substring(begin, end)
            case 2: {
                JawaObjectRef begin = currentActivation.getLast().get("begin");
                if (!(begin.object instanceof Double))
                    throw new JawascriptRuntimeException("The first argument of substring must be a number");
                int beginInt = ((Double) begin.object).intValue();
                if (beginInt < 0 || beginInt >= str.length())
                    throw new JawascriptRuntimeException("begin out of bound.");
                if (currentActivation.getLast().get("end") == null)
                    return new JawaObjectRef(str.substring(beginInt));
                else {
                    JawaObjectRef end = currentActivation.getLast().get("end");
                    if (!(end.object instanceof Double))
                        throw new JawascriptRuntimeException("The second argument of substring must be a number");
                    int endInt = ((Double) end.object).intValue();
                    if (endInt < 0 || endInt > str.length())
                        throw new JawascriptRuntimeException("end out of bound.");
                    return new JawaObjectRef(str.substring(beginInt, endInt));
                }
            }
            // String.toLowerCase()
            case 3: {
                return new JawaObjectRef(str.toString().toLowerCase());
            }
            // String.replace()
            case 4: {
                JawaObjectRef searchvalue = currentActivation.getLast().get("searchvalue");
                JawaObjectRef newvalue = currentActivation.getLast().get("newvalue");
                return new JawaObjectRef(str.toString().replaceAll(searchvalue.toString(), newvalue.toString()));
            }
            // String.charCodeAt(index)
            case 5: {
                JawaObjectRef index = currentActivation.getLast().get("index");
                if (!(index.object instanceof Double))
                    throw new JawascriptRuntimeException("The index of charCodeAt must be a number");
                int indexInt = ((Double) index.object).intValue();
                if (indexInt < 0 || indexInt >= str.length())
                    throw new JawascriptRuntimeException("index out of bound.");
                return new JawaObjectRef(str.charAt(indexInt));
            }
            // String.indexOf(searchvalue, start)
            case 6: {
                String searchvalue = currentActivation.getLast().get("searchvalue").toString();
                int startInt = 0;
                JawaObjectRef start = currentActivation.getLast().get("start");
                if (start != null) {
                    if (!(start.object instanceof Double))
                        throw new JawascriptRuntimeException("The start of indexOf must be a number");
                    startInt = ((Double) start.object).intValue();
                    if (startInt < 0 || startInt >= str.length())
                        throw new JawascriptRuntimeException("start out of bound.");
                }
                return new JawaObjectRef(str.indexOf(searchvalue, startInt));
            }
            // String.lastIndexOf(searchvalue, start)
            case 7: {
                String searchvalue = currentActivation.getLast().get("searchvalue").toString();
                int startInt = str.length();
                JawaObjectRef start = currentActivation.getLast().get("start");
                if (start != null) {
                    if (!(start.object instanceof Double))
                        throw new JawascriptRuntimeException("The start of indexOf must be a number");
                    startInt = ((Double) start.object).intValue();
                    if (startInt < 0 || startInt >= str.length())
                        throw new JawascriptRuntimeException("start out of bound.");
                }
                return new JawaObjectRef(str.lastIndexOf(searchvalue, startInt));
            }
            // String.trim()
            case 8: {
                return new JawaObjectRef(str.toString().trim());
            }

            default:
                throw new JawascriptRuntimeException("Not yet implemented.");
        }
    }
    ////////////////////////////////////////////////////

    private HashMap<String, JawaFunc> builtinFunctions = new HashMap<String, JawaFunc>();
    private JawaObjectRef dispatchBuiltin(String funcName) throws JawascriptRuntimeException, JSONException {
        if (builtinFunctions.get(funcName) == null)
            throw new JawascriptRuntimeException("No method " + funcName + "().");
        int funcId = builtinFunctions.get(funcName).switchId;
        // Assumption: The following sequence must be as same as the registering sequence.

        switch(funcId) {
            // alert()
            case 0: {
                JawaObjectRef msg = activations.getLast().getLast().get("msg");
                if (msg == null)
                    System.out.println("undefined");
                else
                    System.out.println(msg.toString());
                return null;
            }
            // getenv(varname)
            case 1: {
                String varname = currentActivation.getLast().get("varname").toString();
                if (env.has(varname)) {
                    Object ret = env.opt(varname);
                    return toJawa(ret);
                } else
                    return null;

            }
            // extern(functionName, argument)
            case 2: {
                String functionName = currentActivation.getLast().get("functionName").toString();
                JawaObjectRef arg = currentActivation.getLast().get("argument");
                JSONObject argJson = null;
                if (arg != null) {
                    if (!(arg.object instanceof JawaObject))
                        throw new JawascriptRuntimeException("The argument for extern() must be an object");
                    argJson = new JSONObject(((JawaObject) arg.object).toJSON(null).toString());
                }
                return toJawaObject(externalCallback.call(functionName, argJson));
            }
            // parseInt(string, radix)
            case 3: {
                String str = currentActivation.getLast().get("string").toString().trim().toLowerCase();
                if (str.length() == 0)
                    return new JawaObjectRef(0);

                // Sign
                int sign = 1;
                if (str.charAt(0) == '+' || str.charAt(0) == '-') {
                    str = str.substring(1);
                    sign = str.charAt(0) == '+' ? 1 : -1;
                }

                // Radix
                int radix = 10;
                JawaObjectRef arg2 = currentActivation.getLast().get("radix");
                if (arg2 != null && arg2.object instanceof Double)
                    radix = ((Double)arg2.object).intValue();
                else if (str.length() >= 2 && str.charAt(0) == '0' && (str.charAt(1) == 'x' || str.charAt(1) == 'X')) {
                    radix = 16;
                    str = str.substring(2);
                }

                //
                if (radix > 36)
                    throw new JawascriptRuntimeException("Invalid radix : " + radix);
                String validDigits = "0123456789abcdefghijklmnopqrstuvwxyz".substring(0, radix);
                int end;
                for (end = 0 ; end < str.length() ; end++) {
                    char c = str.charAt(end);
                    if (validDigits.indexOf(c) == -1)
                        break;
                }
                str = str.substring(0, end);
                if (str.length() == 0)
                    return new JawaObjectRef(0);

                return new JawaObjectRef(sign * Long.parseLong(str, radix));
            }
            default:
                throw new JawascriptRuntimeException("BuiltIn function not found : " + funcName);
        }
    }
    ////////////////////////////////////////////////////

    private JSONObject env;
    private HashMap<String, JawaObjectRef> global = new HashMap<String, JawaObjectRef>();
    private LinkedList<LinkedList<HashMap<String, JawaObjectRef>>> activations = new LinkedList<LinkedList<HashMap<String, JawaObjectRef>>>();
    private LinkedList<HashMap<String, JawaObjectRef>> currentActivation;
    private HashMap<String, JawaObjectRef> currentIterationScope = null;
    private boolean isFromCallExpression = false;
    private ExternalCallback externalCallback = null;

    private JawaObjectRef evaluate(JSONObject tree) throws JSONException, JawascriptRuntimeException {
        if (tree == null)
            throw new NullPointerException();
        int astType = tree.getInt("t");
        switch(astType) {
            case SCRIPT_BODY:
                return evalScriptBody(tree);
            case FUNCTION_DECLARATION:
                return declareFunction(tree);
            case BLOCK_STATEMENT:
                return evalBlockStatement(tree);
            case EMPTY_STATEMENT:
                return null;
            case SEQUENCE_EXPRESSION:
                return evalSequenceExpression(tree);
            case ASSIGNMENT_EXPRESSION:
                return evalAssignmentExpression(tree);
            case CONDITIONAL_EXPRESSION:
                return evalConditionalExpression(tree);
            case LOGICAL_OR_EXPRESSION:
                return evalLogicalOrExpression(tree);
            case LOGICAL_AND_EXPRESSION:
                return evalLogicalAndExpression(tree);
            case INCLUSIVE_OR_EXPRESSION:
                return evalInclusiveOrExpression(tree);
            case EXCLUSIVE_OR_EXPRESSION:
                return evalExclusiveOrExpression(tree);
            case AND_EXPRESSION:
                return evalAndExpression(tree);
            case EQUALITY_EXPRESSION:
                return evalEqualityExpression(tree);
            case RELATIONAL_EXPRESSION:
                return evalRelationalExpression(tree);
            case IN_EXPRESSION:
                return evalInExpression(tree);
            case SHIFT_EXPRESSION:
                return evalShiftExpression(tree);
            case ADDITIVE_EXPRESSION:
                return evalAdditiveExpression(tree);
            case MULTIPLICATIVE_EXPRESSION:
                return evalMultiplicativeExpression(tree);
            case UNARY_EXPRESSION:
                return evalUnaryExpression(tree);
            case POSTFIX_EXPRESSION:
                return evalPostfixExpression(tree);
            case STATIC_MEMBER_EXPRESSION:
                return evalStaticMemberExpression(tree);
            case CALL_EXPRESSION:
                return evalCallExpression(tree);
            case COMPUTED_MEMBER_EXPRESSION:
                return evalComputedMemberExpression(tree);
            case ARRAY_EXPRESSION:
                return evalArrayExpression(tree);
            case OBJECT_EXPRESSION:
                return evalObjectExpression(tree);
            case IDENTIFIER:
                return resolveIdentifier(tree);
            case IF_STATEMENT:
                return execIfStatement(tree);
            case RETURN_STATEMENT:
                return execReturnStatement(tree);
            case VAR_STATEMENT:
                return execVarStatement(tree);
            case WHILE_STATEMENT:
                return execWhileStatement(tree);
            case CONTINUE_STATEMENT:
                return execContinueStatement();
            case BREAK_STATEMENT:
                return execBreakStatement();
            case DO_WHILE_STATEMENT:
                return execDoWhileStatement(tree);
            case FOR_STATEMENT:
                return execForStatement(tree);
            case VARIABLE_DECLARATION:
                return declareVar(tree);
            case LITERAL:
                return evalLiteral(tree);
            default:
                throw new JawascriptRuntimeException("Not yet implemented: " + astType);
        }
    }

    private void placeReturn(JawaObjectRef retValue) {
        currentActivation.getFirst().put("return", retValue);
    }

    private JawaObjectRef evalScriptBody(JSONObject ast) throws JSONException, JawascriptRuntimeException {
        //System.out.println("Running SCRIPT_BODY");
        JSONArray statements = getArray(ast, PR_statements);
        for (int i = 0 ; i < statements.length() ; i++) {
            JSONObject statement = statements.getJSONObject(i);
            evaluate(statement);
        }
        return null;
    }

    private JawaObjectRef evalSequenceExpression(JSONObject ast) throws JSONException, JawascriptRuntimeException {
        //System.out.println("Running SEQUENCE_EXPRESSION");
        JSONArray expressions = getArray(ast, PR_expressions);
        JawaObjectRef ret = null;
        for (int i = 0 ; i < expressions.length() ; i++) {
            JSONObject expression = expressions.getJSONObject(i);
            ret = evaluate(expression);
        }
        return ret;
    }

    private JawaObjectRef evalAssignmentExpression(JSONObject ast) throws JSONException, JawascriptRuntimeException {
        //System.out.println("Running ASSIGNMENT_EXPRESSION");
        JawaObjectRef left = evaluate(getObj(ast, PR_left));
        String op = getString(ast, PR_op).split(",")[1];
        JawaObjectRef right = evaluate(getObj(ast, PR_right));

        if (op.equals("=")) {
            if (left != null) {
                left.object = right != null ? right.transfer() : null;
                return left;
            } else {
                JSONObject leftExpr = getObj(ast, PR_left);
                JawaObjectRef obj = evaluate(getObj(leftExpr, PR_object));
                if (obj != null) {
                    int t = leftExpr.getInt("t");
                    if (t == STATIC_MEMBER_EXPRESSION) {
                        if (obj.object instanceof JawaObject) {
                            String property = getString(getObj(leftExpr, PR_property), PR_id);
                            ((JawaObject) obj.object).setProp(property, right);
                            return right;
                        }
                    } else if (t == COMPUTED_MEMBER_EXPRESSION) {
                        if (obj.object instanceof JawaObject) {
                            JawaObjectRef computed = evaluate(getObj(leftExpr, PR_property));
                            if (computed == null)
                                throw new JawascriptRuntimeException("Computed member for object is null.");
                            String property = computed.toString();
                            ((JawaObject) obj.object).setProp(property, right);
                            return right;
                        }
                    }
                }
            }
            throw new JawascriptRuntimeException("Left operand of = is null.");
        } else if (op.equals("+=")) {
            if (left == null)
                throw new JawascriptRuntimeException("Left operand of += is null.");
            if (right == null)
                throw new JawascriptRuntimeException("Right operand of += is null.");

            if (left.object instanceof StringBuilder) {
                left.object = (new JawaObjectRef(left.object.toString() + right.toString())).transfer();
            } else if (left.object instanceof Double && right.object instanceof Double) {
                left.object = (new JawaObjectRef((Double)left.object + (Double)right.object)).transfer();
            } else
                throw new JawascriptRuntimeException("Invalid type for +=");
        } else if (op.equals("-=") || op.equals("*=") || op.equals("/=") ||
                op.equals("%=")) {
            if (left == null)
                throw new JawascriptRuntimeException("Left operand of -=,*=,/=,%= is null.");
            if (right == null)
                throw new JawascriptRuntimeException("Right operand of -=,*=,/=,%= is null.");

            if (left.object instanceof Double && right.object instanceof Double) {
                double l = (Double)left.object;
                double r = (Double)right.object;
                switch(op.charAt(0)) {
                    case '-':
                        left.object = new JawaObjectRef(l - r).transfer();
                        break;
                    case '*':
                        left.object = new JawaObjectRef(l * r).transfer();
                        break;
                    case '/':
                        left.object = new JawaObjectRef(l / r).transfer();
                        break;
                    case '%':
                        left.object = new JawaObjectRef(l % r).transfer();
                        break;
                }
            } else
                throw new JawascriptRuntimeException("Invalid type for -=,*=,/=,%=");
        } else if (op.equals("|=") || op.equals("&=")) {
            if (left == null)
                throw new JawascriptRuntimeException("Left operand of |=,&= is null.");
            if (right == null)
                throw new JawascriptRuntimeException("Right operand of |=,&= is null.");

            if (left.object instanceof Double && right.object instanceof Double) {
                int l = toInteger(left);
                int r = toInteger(right);
                switch(op.charAt(0)) {
                    case '&':
                        left.object = new JawaObjectRef(l & r).transfer();
                        break;
                    case '|':
                        left.object = new JawaObjectRef(l | r).transfer();
                        break;
                }
            } else
                throw new JawascriptRuntimeException("Invalid type for |=,&=");
        }else
            throw new JawascriptRuntimeException("Not implemented yet: " + op);
        return null;
    }

    private JawaObjectRef evalConditionalExpression(JSONObject ast) throws JSONException, JawascriptRuntimeException {
        //System.out.println("Running CONDITIONAL_EXPRESSION");
        JawaObjectRef test = evaluate(getObj(ast, PR_condition));
        if (test == null)
            return evaluate(getObj(ast, PR_onFalse));

        if (test.object instanceof Boolean) {
            if ((Boolean)test.object) {
                return evaluate(getObj(ast, PR_onTrue));
            } else {
                return evaluate(getObj(ast, PR_onFalse));
            }
        } else
            throw new JawascriptRuntimeException("Not yet implemented.");
    }

    private JawaObjectRef evalLogicalOrExpression(JSONObject ast) throws JSONException, JawascriptRuntimeException {
        //System.out.println("Running LOGICAL_OR_EXPRESSION");
        JSONArray oprnds = getArray(ast, PR_subExpressions);

        for (int i = 0 ; i < oprnds.length() ; i++) {
            JawaObjectRef oprnd = evaluate(oprnds.getJSONObject(i));
            if (oprnd == null || oprnd.object instanceof Boolean && (Boolean)oprnd.object ||
                    oprnd.object instanceof StringBuilder && !oprnd.object.toString().isEmpty() ||
                    oprnd.object instanceof Double && (Double)oprnd.object != 0 ||
                    oprnd.object instanceof JawaFunc ||
                    oprnd.object instanceof JawaArray ||
                    oprnd.object instanceof JawaObject)
                return oprnd;
        }
        return new JawaObjectRef(false);
    }

    private JawaObjectRef evalLogicalAndExpression(JSONObject ast) throws JSONException, JawascriptRuntimeException {
        //System.out.println("Running LOGICAL_AND_EXPRESSION");
        JSONArray oprnds = getArray(ast, PR_subExpressions);

        for (int i = 0 ; i < oprnds.length() ; i++) {
            JawaObjectRef oprnd = evaluate(oprnds.getJSONObject(i));
            if (oprnd == null || oprnd.object instanceof Boolean && !(Boolean)oprnd.object ||
                    oprnd.object instanceof StringBuilder ||
                    oprnd.object instanceof Double ||
                    oprnd.object instanceof JawaFunc ||
                    oprnd.object instanceof JawaArray ||
                    oprnd.object instanceof JawaObject)
                return oprnd;
        }
        return new JawaObjectRef(true);
    }

    private JawaObjectRef evalInclusiveOrExpression(JSONObject ast) throws JSONException, JawascriptRuntimeException {
        //System.out.println("Running INCLUSIVE_OR_EXPRESSION");
        JSONArray oprnds = getArray(ast, PR_subExpressions);

        JawaObjectRef firstOprnd = evaluate(oprnds.getJSONObject(0));
        for (int i = 1 ; i < oprnds.length() ; i++) {
            JawaObjectRef secondOprnd = evaluate(oprnds.getJSONObject(i));

            if (firstOprnd == null || secondOprnd == null ||
                    !(firstOprnd.object instanceof Double && secondOprnd.object instanceof Double))
                throw new JawascriptRuntimeException("| applies to only numbers");
            int l = toInteger(firstOprnd);
            int r = toInteger(secondOprnd);
            firstOprnd = new JawaObjectRef(l | r);
        }
        return firstOprnd;
    }

    private JawaObjectRef evalExclusiveOrExpression(JSONObject ast) throws JSONException, JawascriptRuntimeException {
        //System.out.println("Running EXCLUSIVE_OR_EXPRESSION");
        JSONArray oprnds = getArray(ast, PR_subExpressions);

        JawaObjectRef firstOprnd = evaluate(oprnds.getJSONObject(0));
        for (int i = 1 ; i < oprnds.length() ; i++) {
            JawaObjectRef secondOprnd = evaluate(oprnds.getJSONObject(i));

            if (firstOprnd == null || secondOprnd == null ||
                    !(firstOprnd.object instanceof Double && secondOprnd.object instanceof Double))
                throw new JawascriptRuntimeException("^ applies to only numbers");
            int l = toInteger(firstOprnd);
            int r = toInteger(secondOprnd);
            firstOprnd = new JawaObjectRef(l ^ r);
        }
        return firstOprnd;
    }

    private JawaObjectRef evalAndExpression(JSONObject ast) throws JSONException, JawascriptRuntimeException {
        //System.out.println("Running AND_EXPRESSION");
        JSONArray oprnds = getArray(ast, PR_subExpressions);

        JawaObjectRef firstOprnd = evaluate(oprnds.getJSONObject(0));
        for (int i = 1 ; i < oprnds.length() ; i++) {
            JawaObjectRef secondOprnd = evaluate(oprnds.getJSONObject(i));

            if (firstOprnd == null || secondOprnd == null ||
                    !(firstOprnd.object instanceof Double && secondOprnd.object instanceof Double))
                throw new JawascriptRuntimeException("& applies to only numbers");
            int l = toInteger(firstOprnd);
            int r = toInteger(secondOprnd);
            firstOprnd = new JawaObjectRef(l & r);
        }
        return firstOprnd;
    }

    private JawaObjectRef evalEqualityExpression(JSONObject ast) throws JSONException, JawascriptRuntimeException {
        //System.out.println("Running EQUALITY_EXPRESSION");
        JSONArray ops = getArray(ast, PR_ops);
        JSONArray oprnds = getArray(ast, PR_subExpressions);
        if (ops.length() < 1 || oprnds.length() != ops.length() + 1)
            throw new JawascriptRuntimeException("Invalid equality expression");
        JawaObjectRef firstOprnd = evaluate(oprnds.getJSONObject(0));
        for (int i = 1 ; i < oprnds.length() ; i++) {
            JawaObjectRef secondOprnd = evaluate(oprnds.getJSONObject(i));
            String op = ops.getJSONObject(i - 1).getString("v");
            boolean inverse = op.startsWith("!");
            if (op.equals("==") || op.equals("!=")) {
                boolean result;
                if (firstOprnd == null)
                    firstOprnd = new JawaObjectRef();
                if (secondOprnd == null)
                    secondOprnd = new JawaObjectRef();

                if (firstOprnd.object == null || secondOprnd.object == null) {
                    result = (firstOprnd.object == secondOprnd.object);
                }
                else if (firstOprnd.object instanceof StringBuilder && secondOprnd.object instanceof StringBuilder)
                    result = firstOprnd.object.toString().equals(secondOprnd.object.toString());
                else if (firstOprnd.object instanceof Double && secondOprnd.object instanceof Double)
                    result = ((Double)firstOprnd.object).doubleValue() == ((Double)secondOprnd.object).doubleValue();
                else if (firstOprnd.object instanceof Boolean && secondOprnd.object instanceof Boolean)
                    result = ((Boolean)firstOprnd.object).booleanValue() == ((Boolean)secondOprnd.object).booleanValue();
                else if (firstOprnd.object instanceof JawaFunc ||
                        firstOprnd.object instanceof JawaArray ||
                        firstOprnd.object instanceof JawaObject ||
                        secondOprnd.object instanceof JawaFunc ||
                        secondOprnd.object instanceof JawaArray ||
                        secondOprnd.object instanceof JawaObject)
                    result = (firstOprnd.object == secondOprnd.object);
                else {
                    result = false;
                }
                firstOprnd = new JawaObjectRef(inverse ? !result : result);
            } else {
                throw new JawascriptRuntimeException("Not yet implemented : " + op);
            }
        }
        return firstOprnd;
    }

    private JawaObjectRef evalRelationalExpression(JSONObject ast) throws JSONException, JawascriptRuntimeException {
        //System.out.println("Running RELATIONAL_EXPRESSION");
        JSONArray ops = getArray(ast, PR_ops);
        JSONArray oprnds = getArray(ast, PR_subExpressions);
        if (ops.length() < 1 || oprnds.length() != ops.length() + 1)
            throw new JawascriptRuntimeException("Invalid relational expression");
        JawaObjectRef firstOprnd = evaluate(oprnds.getJSONObject(0));
        for (int i = 1 ; i < oprnds.length() ; i++) {
            JawaObjectRef secondOprnd = evaluate(oprnds.getJSONObject(i));
            String op = ops.getJSONObject(i - 1).getString("v");
            if (firstOprnd == null || firstOprnd.object == null)
                firstOprnd = new JawaObjectRef(0);
            if (secondOprnd == null || secondOprnd.object == null)
                secondOprnd = new JawaObjectRef(0);
            if (op.equals("<")) {
                if (firstOprnd.object instanceof StringBuilder && secondOprnd.object instanceof StringBuilder) {
                    firstOprnd = new JawaObjectRef(firstOprnd.object.toString().compareTo(secondOprnd.object.toString()) < 0);
                } else if (firstOprnd.object instanceof Double && secondOprnd.object instanceof Double) {
                    firstOprnd = new JawaObjectRef((Double)firstOprnd.object < (Double)secondOprnd.object);
                } else
                    throw new JawascriptRuntimeException("Invalid type for operator <");
            } else if (op.equals(">")) {
                if (firstOprnd.object instanceof StringBuilder && secondOprnd.object instanceof StringBuilder) {
                    firstOprnd = new JawaObjectRef(firstOprnd.object.toString().compareTo(secondOprnd.object.toString()) > 0);
                } else if (firstOprnd.object instanceof Double && secondOprnd.object instanceof Double) {
                    firstOprnd = new JawaObjectRef((Double)firstOprnd.object > (Double)secondOprnd.object);
                } else
                    throw new JawascriptRuntimeException("Invalid type for operator >");
            } else if (op.equals("<=")) {
                if (firstOprnd.object instanceof StringBuilder && secondOprnd.object instanceof StringBuilder) {
                    firstOprnd = new JawaObjectRef(firstOprnd.object.toString().compareTo(secondOprnd.object.toString()) <= 0);
                } else if (firstOprnd.object instanceof Double && secondOprnd.object instanceof Double) {
                    firstOprnd = new JawaObjectRef((Double)firstOprnd.object <= (Double)secondOprnd.object);
                } else
                    throw new JawascriptRuntimeException("Invalid type for operator <=");
            } else if (op.equals(">=")) {
                if (firstOprnd.object instanceof StringBuilder && secondOprnd.object instanceof StringBuilder) {
                    firstOprnd = new JawaObjectRef(firstOprnd.object.toString().compareTo(secondOprnd.object.toString()) >= 0);
                } else if (firstOprnd.object instanceof Double && secondOprnd.object instanceof Double) {
                    firstOprnd = new JawaObjectRef((Double)firstOprnd.object >= (Double)secondOprnd.object);
                } else
                    throw new JawascriptRuntimeException("Invalid type for operator >=");
            } else {
                throw new JawascriptRuntimeException("Not yet implemented : " + op);
            }
        }
        return firstOprnd;
    }

    private JawaObjectRef evalInExpression(JSONObject ast) throws JSONException, JawascriptRuntimeException {
        //System.out.println("Running IN_EXPRESSION");
        JSONArray oprnds = getArray(ast, PR_subExpressions);

        JawaObjectRef firstOprnd = evaluate(oprnds.getJSONObject(0));
        for (int i = 1 ; i < oprnds.length() ; i++) {
            JawaObjectRef secondOprnd = evaluate(oprnds.getJSONObject(i));

            if (secondOprnd == null)
                throw new JawascriptRuntimeException("Second operand of in expression mustn't be null");
            if (firstOprnd == null) {
                firstOprnd = new JawaObjectRef(false);
                continue;
            }

            boolean found;
            if (secondOprnd.object instanceof JawaArray) {
                JawaArray arr = (JawaArray) secondOprnd.object;
                if (firstOprnd.object instanceof Double) {
                    double value = (Double)firstOprnd.object;
                    if (Math.abs(Math.round(value) - value) < QUANTUM) {
                        int index = (int)Math.round(value);
                        found = index >= 0 && index < arr.elements.size();
                    } else
                        found = false;
                } else
                    found = arr.getProp(firstOprnd.toString()) != null;
            } else if (secondOprnd.object instanceof JawaObject) {
                found = ((JawaObject)secondOprnd.object).getProp(firstOprnd.toString()) != null;
            } else
                throw new JawascriptRuntimeException("Illegal operand for in operator");

            firstOprnd = new JawaObjectRef(found);
        }
        return firstOprnd;
    }

    private JawaObjectRef evalShiftExpression(JSONObject ast) throws JSONException, JawascriptRuntimeException {
        //System.out.println("Running SHIFT_EXPRESSION");
        JSONArray ops = getArray(ast, PR_ops);
        JSONArray oprnds = getArray(ast, PR_subExpressions);
        if (ops.length() < 1 || oprnds.length() != ops.length() + 1)
            throw new JawascriptRuntimeException("Invalid shift expression");
        JawaObjectRef firstOprnd = evaluate(oprnds.getJSONObject(0));
        for (int i = 1 ; i < oprnds.length() ; i++) {
            JawaObjectRef secondOprnd = evaluate(oprnds.getJSONObject(i));
            String op = ops.getJSONObject(i - 1).getString("v");
            if (firstOprnd == null || secondOprnd == null)
                throw new JawascriptRuntimeException("Shift ops cannot have null operands");
            if (op.equals(">>")) {
                if (firstOprnd.object instanceof Double && secondOprnd.object instanceof Double) {
                    long shifted = ((Double) firstOprnd.object).longValue();
                    int shift = ((Double) secondOprnd.object).intValue();
                    firstOprnd = new JawaObjectRef(shifted >> shift);
                } else
                    throw new JawascriptRuntimeException("Invalid type for shift op");
            } else if (op.equals("<<")) {
                if (firstOprnd.object instanceof Double && secondOprnd.object instanceof Double) {
                    long shifted = ((Double) firstOprnd.object).longValue();
                    int shift = ((Double) secondOprnd.object).intValue();
                    firstOprnd = new JawaObjectRef((int)(shifted << shift));
                } else
                    throw new JawascriptRuntimeException("Invalid type for shift op");
            } else if (op.equals(">>>")) {
                if (firstOprnd.object instanceof Double && secondOprnd.object instanceof Double) {
                    long shifted = ((Double) firstOprnd.object).longValue();
                    int shift = ((Double) secondOprnd.object).intValue();
                    firstOprnd = new JawaObjectRef((int)(shifted) >>> shift);
                } else
                    throw new JawascriptRuntimeException("Invalid type for shift op");
            }else {
                throw new JawascriptRuntimeException("Not yet implemented : " + op);
            }
        }
        return firstOprnd;
    }

    private JawaObjectRef evalAdditiveExpression(JSONObject ast) throws JSONException, JawascriptRuntimeException {
        //System.out.println("Running ADDITIVE_EXPRESSION");
        JSONArray ops = getArray(ast, PR_ops);
        JSONArray oprnds = getArray(ast, PR_subExpressions);
        if (ops.length() < 1 || oprnds.length() != ops.length() + 1)
            throw new JawascriptRuntimeException("Invalid additive expression");
        JawaObjectRef firstOprnd = evaluate(oprnds.getJSONObject(0));
        for (int i = 1 ; i < oprnds.length() ; i++) {
            JawaObjectRef secondOprnd = evaluate(oprnds.getJSONObject(i));
            String op = ops.getJSONObject(i - 1).getString("v");
            if (firstOprnd == null || secondOprnd == null)
                throw new JawascriptRuntimeException("Additive ops cannot have null operands");
            if (op.equals("+")) {
                if (firstOprnd.object instanceof StringBuilder || secondOprnd.object instanceof StringBuilder) {
                    firstOprnd = new JawaObjectRef(firstOprnd.toString() + secondOprnd.toString());
                } else if (firstOprnd.object instanceof Double && secondOprnd.object instanceof Double) {
                    firstOprnd = new JawaObjectRef((Double) firstOprnd.object + (Double) secondOprnd.object);
                } else
                    throw new JawascriptRuntimeException("Invalid type for +");
            } else if (op.equals("-")) {
                if (firstOprnd.object instanceof Double && secondOprnd.object instanceof Double) {
                    firstOprnd = new JawaObjectRef((Double) firstOprnd.object - (Double) secondOprnd.object);
                } else
                    throw new JawascriptRuntimeException("Invalid type for -");
            } else {
                throw new JawascriptRuntimeException("Not yet implemented : " + op);
            }
        }
        return firstOprnd;
    }

    private JawaObjectRef evalMultiplicativeExpression(JSONObject ast) throws JSONException, JawascriptRuntimeException {
        //System.out.println("Running MULTIPLICATIVE_EXPRESSION");
        JSONArray ops = getArray(ast, PR_ops);
        JSONArray oprnds = getArray(ast, PR_subExpressions);
        if (ops.length() < 1 || oprnds.length() != ops.length() + 1)
            throw new JawascriptRuntimeException("Invalid multiplicative expression");
        JawaObjectRef firstOprnd = evaluate(oprnds.getJSONObject(0));
        for (int i = 1 ; i < oprnds.length() ; i++) {
            JawaObjectRef secondOprnd = evaluate(oprnds.getJSONObject(i));
            String op = ops.getJSONObject(i - 1).getString("v");
            if (firstOprnd == null || secondOprnd == null)
                throw new JawascriptRuntimeException("Multiplicative ops cannot have null operands");
            if (op.equals("*")) {
                if (firstOprnd.object instanceof Double && secondOprnd.object instanceof Double) {
                    firstOprnd = new JawaObjectRef((Double) firstOprnd.object * (Double) secondOprnd.object);
                } else
                    throw new JawascriptRuntimeException("Invalid type for *");
            } else if (op.equals("/")) {
                if (firstOprnd.object instanceof Double && secondOprnd.object instanceof Double) {
                    if (((Double) secondOprnd.object) == 0)
                        throw new JawascriptRuntimeException("Divided by zero");
                    firstOprnd = new JawaObjectRef((Double) firstOprnd.object / (Double) secondOprnd.object);
                } else
                    throw new JawascriptRuntimeException("Invalid type for /");
            } else if (op.equals("%")) {
                if (firstOprnd.object instanceof Double && secondOprnd.object instanceof Double) {
                    if (((Double) secondOprnd.object) == 0)
                        throw new JawascriptRuntimeException("Divided by zero");
                    firstOprnd = new JawaObjectRef((Double) firstOprnd.object % (Double) secondOprnd.object);
                } else
                    throw new JawascriptRuntimeException("Invalid type for %");
            } else {
                throw new JawascriptRuntimeException("Not yet implemented : " + op);
            }
        }
        return firstOprnd;
    }

    private JawaObjectRef evalUnaryExpression(JSONObject ast) throws JSONException, JawascriptRuntimeException {
        //System.out.println("Running UNARY_EXPRESSION");
        String op = getString(ast, PR_op).split(",")[1];
        JawaObjectRef subExpression = evaluate(getObj(ast, PR_subExpression));
        if (subExpression == null)
            throw new JawascriptRuntimeException("Unary op cannot be apply to null");

        if (op.equals("++") || op.equals("--")) {
            if (!(subExpression.object instanceof Double))
                throw new JawascriptRuntimeException("++ and -- only apply to numbers");
            subExpression.object = ((Double) subExpression.object) + (op.equals("++") ? 1 : -1);
            return subExpression;
        } else if (op.equals("-")) {
            if (subExpression.object instanceof Double)
                return new JawaObjectRef( -(Double) subExpression.object );
            else
                throw new JawascriptRuntimeException("Not implemented yet.");
        } else if (op.equals("~")) {
            if (subExpression.object instanceof Double) {
                int truncated = toInteger(subExpression);
                return new JawaObjectRef(~truncated);
            } else
                throw new JawascriptRuntimeException("Not implemented yet.");
        } else if (op.equals("!")) {
            if (subExpression.object instanceof Boolean) {
                return new JawaObjectRef(!(Boolean) subExpression.object);
            } else
                throw new JawascriptRuntimeException("Not implemented yet.");
        }
        return null;
    }

    private JawaObjectRef evalPostfixExpression(JSONObject ast) throws JSONException, JawascriptRuntimeException {
        //System.out.println("Running POSTFIX_EXPRESSION");
        JawaObjectRef subExpression = evaluate(getObj(ast, PR_subExpression));
        if (subExpression == null)
            throw new JawascriptRuntimeException("Postfix op cannot be apply to null");

        if (!(subExpression.object instanceof Double))
            throw new JawascriptRuntimeException("++ and -- only apply to numbers");
        JawaObjectRef ret = new JawaObjectRef((Double)subExpression.object);
        String op = getString(ast, PR_op).split(",")[1];
        if (op.equals("++"))
            subExpression.object = (Double)subExpression.object + 1;
        else if (op.equals("--"))
            subExpression.object = (Double)subExpression.object - 1;
        else
            throw new JawascriptRuntimeException("Invalid postfix operator");

        return ret;
    }

    private JawaObjectRef evalStaticMemberExpression(JSONObject ast) throws JSONException, JawascriptRuntimeException {
        //System.out.println("Running STATIC_MEMBER_EXPRESSION");
        JawaObjectRef object = evaluate(getObj(ast, PR_object));
        String property = getString(getObj(ast, PR_property), PR_id);
        if (object == null || object.object == null)
            throw new JawascriptRuntimeException("Null cannot have any properties.");
        if (object.object instanceof JawaObject) {
            JawaObjectRef prop = ((JawaObject) object.object).getProp(property);
            if (prop == null || !(prop.object instanceof JawaFunc))
                return prop;
            if (((JawaFunc)prop.object).isPropertyWrapper)
                return ((JawaFunc)prop.object).apply(object);
            else {
                return prop;
            }
        } else if (object.object instanceof StringBuilder) {
            JawaFunc func = stringPrototype.get(property);
            if (func.isPropertyWrapper)
                return func.apply(object);
            else
                return new JawaObjectRef(func, object);
        } else {
            throw new JawascriptRuntimeException("Not implemented yet");
        }
    }

    private JawaObjectRef evalCallExpression(JSONObject ast) throws JSONException, JawascriptRuntimeException {
        //System.out.println("Running CALL_EXPRESSION");
        JSONObject function = getObj(ast, PR_function);
        JawaObjectRef object = evaluate(function);
        if (object == null)
            throw new JawascriptRuntimeException("Undefined function : " + ast.toString());
        if (!(object.object instanceof JawaFunc))
            throw new JawascriptRuntimeException("Call operator must be applied to function");
        JawaFunc resolvedFunction = (JawaFunc)(object.object);
        JSONArray arguments = getArray(getObj(ast, PR_arguments), PR_arguments);
        if (resolvedFunction.params.size() < arguments.length())
            throw new JawascriptRuntimeException("Arguments more than parameters.");

        HashMap<String, JawaObjectRef> scope = new HashMap<String, JawaObjectRef>();
        for (int i = 0 ; i < arguments.length() ; i++) {
            JSONObject argument = arguments.getJSONObject(i);
            JawaObjectRef evaluated = evaluate(argument);
            String paramName = resolvedFunction.params.get(i);
            scope.put(paramName, evaluated);
        }

        LinkedList<HashMap<String, JawaObjectRef>> activation = new LinkedList<HashMap<String, JawaObjectRef>>();
        activation.addLast(scope);
        boolean oldIsFromCallExpression = isFromCallExpression;
        isFromCallExpression = true;
        activations.addLast(activation);
        currentActivation = activation;
        JawaObjectRef ret;
        if (object.self == null)
            ret = resolvedFunction.apply();
        else
            ret = resolvedFunction.apply(object.self);
        activations.removeLast();
        currentActivation = activations.getLast();
        isFromCallExpression = oldIsFromCallExpression;
        return ret;
    }

    private JawaObjectRef evalComputedMemberExpression(JSONObject ast) throws JSONException, JawascriptRuntimeException {
        //System.out.println("Running COMPUTED_MEMBER_EXPRESSION");
        JawaObjectRef object = evaluate(getObj(ast, PR_object));
        JawaObjectRef property = evaluate(getObj(ast, PR_property));
        if (object == null || object.object == null)
            throw new JawascriptRuntimeException("Null cannot have any properties.");
        if (property == null || property.object == null)
            throw new JawascriptRuntimeException("Property name cannot compute to null.");
        if (object.object instanceof JawaArray) {
            if (property.object instanceof Double) {
                double value = (Double) property.object;
                if (Math.abs(Math.round(value) - value) < QUANTUM) {
                    long index = Math.round(value);
                    if (index > Integer.MAX_VALUE || index < 0)
                        return null;
                    return ((JawaArray) object.object).at((int) index);
                } else {
                    return null;
                }
            } else {
                return ((JawaArray)object.object).getProp(property.toString());
            }
        } else if (object.object instanceof StringBuilder) {
            if (property.object instanceof Double) {
                double value = (Double) property.object;
                if (Math.abs(Math.round(value) - value) < QUANTUM) {
                    long index = Math.round(value);
                    if (index > Integer.MAX_VALUE || index < 0)
                        return null;
                    int indexInt = (int)index;
                    return new JawaObjectRef(object.toString().substring(indexInt, indexInt + 1));
                } else {
                    return null;
                }
            } else
                return new JawaObjectRef(stringPrototype.get(property.toString()));
        } else {
            return ((JawaObject)object.object).getProp(property.toString());
        }
    }

    private JawaObjectRef evalBlockStatement(JSONObject ast) throws JSONException, JawascriptRuntimeException {
        //System.out.println("Running BLOCK_STATEMENT");
        boolean oldIsFromCallExpression = isFromCallExpression;
        if (!isFromCallExpression) {
            // Create a new scope
            currentActivation.addLast(new HashMap<String, JawaObjectRef>());
        }
        isFromCallExpression = false;

        JSONArray statements = getArray(ast, PR_statements);
        for (int i = 0 ; i < statements.length() ; i++) {
            JSONObject statement = statements.getJSONObject(i);

            evaluate(statement);
            if (currentActivation.getFirst().get("return") != null)
                break;
            if (currentIterationScope != null &&
                    (currentIterationScope.get("break") != null ||
                            currentIterationScope.get("continue") != null))
                break;
        }

        isFromCallExpression = oldIsFromCallExpression;
        if (!isFromCallExpression)
            currentActivation.removeLast();
        return null;
    }

    private JawaObjectRef evalArrayExpression(JSONObject ast) throws JSONException, JawascriptRuntimeException {
        JSONArray elements = getArray(ast, PR_elements);
        JawaArray ret = new JawaArray();

        for (int i = 0 ; i < elements.length() ; i++) {
            JawaObjectRef element = evaluate(elements.getJSONObject(i));
            JawaObjectRef obj = new JawaObjectRef();
            obj.object = element != null ? element.transfer() : null;
            ret.append(obj);
        }
        return new JawaObjectRef(ret);
    }

    private JawaObjectRef evalObjectExpression(JSONObject ast) throws JSONException, JawascriptRuntimeException {
        JSONArray properties = getArray(ast, PR_properties);
        JawaObject ret = new JawaObject();

        for (int i = 0 ; i < properties.length() ; i++) {
            JSONObject prop = properties.getJSONObject(i);
            String key = getString(prop, PR_key);
            JawaObjectRef expr = evaluate(getObj(prop, PR_expr));
            JawaObjectRef obj = new JawaObjectRef();
            obj.object = expr != null ? expr.transfer() : null;
            ret.setProp(key, obj);
        }

        return new JawaObjectRef(ret);
    }

    private JawaObjectRef resolveIdentifier(JSONObject ast) throws JSONException, JawascriptRuntimeException {
        String id = getString(ast, PR_id);
        // Search in the current activation. innermost scope first
        Iterator<HashMap<String, JawaObjectRef>> iter = currentActivation.descendingIterator();
        while (iter.hasNext()) {
            HashMap<String, JawaObjectRef> scope = iter.next();
            JawaObjectRef ret = scope.get(id);
            if (ret != null)
                return ret;
        }

        // Search the global environment
        JawaObjectRef ret = global.get(id);
        if (ret != null)
            return ret;

        throw new JawascriptRuntimeException("Unresolvable identifier: " + id);
    }

    private void declare(String id, JawaObjectRef value) throws JawascriptRuntimeException {
        HashMap<String, JawaObjectRef> currentScope = currentActivation.getLast();
        if (currentScope.get(id) != null)
            throw new JawascriptRuntimeException("Variable redeclaration (" + id + ") in the current scope.");
        JawaObjectRef obj = new JawaObjectRef();
        if (value != null)
            obj.object = value.transfer();
        currentScope.put(id, obj);
    }

    private JawaObjectRef declareFunction(JSONObject ast) throws JSONException, JawascriptRuntimeException {
        //System.out.println("Running FUNCTION_DECLARATION");
        String id = getString(ast, PR_id);
        JSONArray params = getArray(ast, PR_params);
        LinkedList<String> param_strs = new LinkedList<String>();
        for (int i = 0 ; i < params.length() ; i++)
            param_strs.addLast(params.getString(i));
        JSONObject body = getObj(ast, PR_body);

        JawaFunc func = new JawaFunc(id, param_strs, false, false, body);
        declare(id, new JawaObjectRef(func));
        return null;
    }

    private JawaObjectRef declareVar(JSONObject ast) throws JSONException, JawascriptRuntimeException {
        //System.out.println("Running VARIABLE_DECLARATION");
        String id = getString(ast, PR_varName);
        JawaObjectRef value = null;
        if (ast.has(Integer.toString(PR_initialization))) {
            JSONObject init = getObj(ast, PR_initialization);
            value = evaluate(init);
        }
        declare(id, value);
        return null;
    }

    private JawaObjectRef execContinueStatement() throws JSONException, JawascriptRuntimeException {
        //System.out.println("Running CONTINUE_STATEMENT");
        currentIterationScope.put("continue", new JawaObjectRef(true));
        return null;
    }

    private JawaObjectRef execBreakStatement() throws JSONException, JawascriptRuntimeException {
        //System.out.println("Running BREAK_STATEMENT");
        currentIterationScope.put("break", new JawaObjectRef(true));
        return null;
    }

    private JawaObjectRef execDoWhileStatement(JSONObject ast) throws JSONException, JawascriptRuntimeException {
        //System.out.println("Running DO_WHILE_STATEMENT");
        JSONObject body = getObj(ast, PR_body);
        JSONObject test = getObj(ast, PR_test);
        HashMap<String, JawaObjectRef> scope = new HashMap<String, JawaObjectRef>();
        currentActivation.addLast(scope);
        HashMap<String, JawaObjectRef> outerIterationScope = currentIterationScope;
        currentIterationScope = scope;

        JawaObjectRef cond;
        do {
            evaluate(body);
            if (currentActivation.getFirst().get("return") != null)
                break;
            if (currentIterationScope.get("break") != null)
                break;
            if (currentIterationScope.get("continue") != null)
                currentIterationScope.remove("continue");
            cond = evaluate(test);
        } while (cond != null && (Boolean)(cond.object));

        currentIterationScope = outerIterationScope;
        currentActivation.removeLast();
        return null;
    }

    private JawaObjectRef execForStatement(JSONObject ast) throws JSONException, JawascriptRuntimeException {
        //System.out.println("Running FOR_STATEMENT");
        JSONObject body = getObj(ast, PR_body);
        HashMap<String, JawaObjectRef> scope = new HashMap<String, JawaObjectRef>();
        currentActivation.addLast(scope);
        HashMap<String, JawaObjectRef> outerIterationScope = currentIterationScope;
        currentIterationScope = scope;

        if (!ast.has(Integer.toString(PR_iterator))) {
            JSONObject test = null;
            JSONObject update = null;
            if (ast.has(Integer.toString(PR_test)))
                test = getObj(ast, PR_test);
            if (ast.has(Integer.toString(PR_update)))
                update = getObj(ast, PR_update);

            if (ast.optJSONArray(Integer.toString(PR_init)) != null) {
                JSONArray init = getArray(ast, PR_init);
                for (int i = 0; i < init.length(); i++)
                    evaluate(init.getJSONObject(i));
            } else if (ast.optJSONObject(Integer.toString(PR_init)) != null){
                JSONObject init = getObj(ast, PR_init);
                evaluate(init);
            }
            JawaObjectRef _T = new JawaObjectRef(true);

            JawaObjectRef cond = test != null ? evaluate(test) : _T;
            while (cond != null && (Boolean)(cond.object)) {
                evaluate(body);
                if (currentActivation.getFirst().get("return") != null)
                    break;
                if (currentIterationScope.get("break") != null)
                    break;
                if (currentIterationScope.get("continue") != null)
                    currentIterationScope.remove("continue");
                if (update != null)
                    evaluate(update);
                cond = test != null ? evaluate(test) : _T;
            }
        } else {
            JSONObject iteratorDeclaration = getObj(ast, PR_iterator);
            String iterator = getString(iteratorDeclaration, PR_varName);
            JawaObjectRef iterable = evaluate(getObj(iteratorDeclaration, PR_iterable));

            if (iterable == null)
                throw new JawascriptRuntimeException("Null is not iterable.");
            if (iterable.object instanceof StringBuilder) {
                String str = iterable.object.toString();
                for (int i = 0 ; i < str.length() ; i++) {
                    scope.put(iterator, new JawaObjectRef(i));
                    evaluate(body);
                    if (currentActivation.getFirst().get("return") != null)
                        break;
                    if (currentIterationScope.get("break") != null)
                        break;
                    if (currentIterationScope.get("continue") != null)
                        currentIterationScope.remove("continue");
                }
            } else if (iterable.object instanceof JawaArray) {
                JawaArray array = (JawaArray)iterable.object;
                for (int i = 0 ; i < array.elements.size() ; i++) {
                    scope.put(iterator, new JawaObjectRef(i));
                    evaluate(body);
                    if (currentActivation.getFirst().get("return") != null)
                        break;
                    if (currentIterationScope.get("break") != null)
                        break;
                    if (currentIterationScope.get("continue") != null)
                        currentIterationScope.remove("continue");
                }
            } else if (iterable.object instanceof JawaObject) {
                JawaObject obj = (JawaObject)iterable.object;
                for (String s : obj.properties.keySet()) {
                    scope.put(iterator, new JawaObjectRef(s));
                    evaluate(body);
                    if (currentActivation.getFirst().get("return") != null)
                        break;
                    if (currentIterationScope.get("break") != null)
                        break;
                    if (currentIterationScope.get("continue") != null)
                        currentIterationScope.remove("continue");
                }
            } else
                throw new JawascriptRuntimeException("Non-iterable object in for-in statement.");
        }

        currentIterationScope = outerIterationScope;
        currentActivation.removeLast();
        return null;
    }

    private JawaObjectRef execIfStatement(JSONObject ast) throws JSONException, JawascriptRuntimeException {
        //System.out.println("Running IF_STATEMENT");
        JawaObjectRef test = evaluate(getObj(ast, PR_test));
        if (test != null && test.object instanceof Boolean) {
            if ((Boolean)test.object) {
                evaluate(getObj(ast, PR_onTrue));
            } else if (ast.has(Integer.toString(PR_onFalse))){
                evaluate(getObj(ast, PR_onFalse));
            }
        } else
            throw new JawascriptRuntimeException("Not yet implemented.");
        return null;
    }

    private JawaObjectRef execReturnStatement(JSONObject ast) throws JSONException, JawascriptRuntimeException {
        //System.out.println("Running RETURN_STATEMENT");
        if (ast.has(Integer.toString(PR_argument))) {
            JawaObjectRef retValue = evaluate(getObj(ast, PR_argument));
            placeReturn(retValue);
        } else {
            placeReturn(null);
        }
        return null;
    }

    private JawaObjectRef execVarStatement(JSONObject ast) throws JSONException, JawascriptRuntimeException {
        //System.out.println("Running VAR_STATEMENT");
        JSONArray declarations = getArray(ast, PR_declarations);
        for (int i = 0 ; i < declarations.length() ; i++) {
            JSONObject declaration = declarations.getJSONObject(i);
            evaluate(declaration);
        }
        return null;
    }

    private JawaObjectRef execWhileStatement(JSONObject ast) throws JSONException, JawascriptRuntimeException {
        //System.out.println("Running WHILE_STATEMENT");
        JSONObject body = getObj(ast, PR_body);
        JSONObject test = getObj(ast, PR_test);
        HashMap<String, JawaObjectRef> scope = new HashMap<String, JawaObjectRef>();
        currentActivation.addLast(scope);
        HashMap<String, JawaObjectRef> outerIterationScope = currentIterationScope;
        currentIterationScope = scope;

        while (true) {
            JawaObjectRef cond = evaluate(test);
            if (cond == null || !(Boolean)cond.object)
                break;
            evaluate(body);
            if (currentActivation.getFirst().get("return") != null)
                break;
            if (currentIterationScope.get("break") != null)
                break;
            if (currentIterationScope.get("continue") != null)
                currentIterationScope.remove("continue");
        }

        currentIterationScope = outerIterationScope;
        currentActivation.removeLast();
        return null;
    }

    private JawaObjectRef evalLiteral(JSONObject ast) throws JSONException, JawascriptRuntimeException {
        //System.out.println("Running LITERAL");
        String literal = getString(ast, PR_literal);
        int sp = literal.indexOf(',');
        String type = literal.substring(0, sp);
        String content = literal.substring(sp + 1);
        if (type.equals("STRING_LITERAL"))
            return new JawaObjectRef(content);
        else if (type.equals("NUMERIC_LITERAL"))
            return new JawaObjectRef(Double.parseDouble(content));
        else if (type.equals("BOOLEAN"))
            return new JawaObjectRef(Boolean.parseBoolean(content));
        else if (type.equals("NULL"))
            return new JawaObjectRef();
        else
            throw new JawascriptRuntimeException("Unknown literal type.");
    }

    private JawaObjectRef toJawa(Object val) {
        if (val instanceof JSONObject)
            return toJawaObject((JSONObject)val);
        else if (val instanceof JSONArray)
            return toJawaArray((JSONArray) val);
        else if (val instanceof String)
            return new JawaObjectRef(val.toString());
        else if (val instanceof Integer)
            return new JawaObjectRef((Integer)val);
        else if (val instanceof Boolean)
            return new JawaObjectRef((Boolean)val);
        else if (val instanceof Double)
            return new JawaObjectRef((Double)val);
        else if (val instanceof Long)
            return new JawaObjectRef((Long)val);
        return null;
    }

    private JawaObjectRef toJawaArray(JSONArray json) {
        JawaArray arr = new JawaArray();
        for (int i = 0 ; i < json.length() ; i++) {
            Object val = json.opt(i);
            arr.append(toJawa(val));
        }
        return new JawaObjectRef(arr);
    }

    private JawaObjectRef toJawaObject(JSONObject json) {
        JawaObject obj = new JawaObject();
        Iterator keys = json.keys();
        while (keys.hasNext()) {
            String key = (String)keys.next();
            Object val = json.opt(key);
            obj.setProp(key, toJawa(val));
        }
        return new JawaObjectRef(obj);
    }

    public Executor()
    {

        currentActivation = new LinkedList<HashMap<String, JawaObjectRef>>();
        currentActivation.addLast(global);
        activations.addLast(currentActivation);

        List<String> VOID = Collections.emptyList();
        registerBuiltinFunc(builtinFunctions, "alert", Collections.singletonList("msg"));
        registerBuiltinFunc(builtinFunctions, "getenv", Collections.singletonList("varname"));
        registerBuiltinFunc(builtinFunctions, "extern", Arrays.asList("functionName", "argument"));
        registerBuiltinFunc(builtinFunctions, "parseInt", Arrays.asList("string", "radix"));
        for (String f : builtinFunctions.keySet()) {
            global.put(f, new JawaObjectRef(builtinFunctions.get(f)));
        }

        registerBuiltinProp(arrayPrototype, "length");
        registerBuiltinFunc(arrayPrototype, "slice", Arrays.asList("start", "end"));
        registerBuiltinFunc(arrayPrototype, "join", Collections.singletonList("sep"));
        registerBuiltinFunc(arrayPrototype, "pop", VOID);
        registerBuiltinFunc(arrayPrototype, "push", Collections.singletonList("item"));
        registerBuiltinFunc(arrayPrototype, "reverse", VOID);
        registerBuiltinFunc(arrayPrototype, "shift", VOID);
        registerBuiltinFunc(arrayPrototype, "sort", Collections.singletonList("compareFunction"));
        registerBuiltinFunc(arrayPrototype, "unshift", Collections.singletonList("item"));

        registerBuiltinFunc(stringPrototype, "split", Collections.singletonList("delim"));
        registerBuiltinProp(stringPrototype, "length");
        registerBuiltinFunc(stringPrototype, "substring", Arrays.asList("begin", "end"));
        registerBuiltinFunc(stringPrototype, "toLowerCase", VOID);
        registerBuiltinFunc(stringPrototype, "replace", Arrays.asList("searchvalue", "newvalue"));
        registerBuiltinFunc(stringPrototype, "charCodeAt", Collections.singletonList("index"));
        registerBuiltinFunc(stringPrototype, "indexOf", Arrays.asList("searchvalue", "start"));
        registerBuiltinFunc(stringPrototype, "lastIndexOf", Arrays.asList("searchvalue", "start"));
        registerBuiltinFunc(stringPrototype, "trim", VOID);

        registerBuiltinFunc(objectPrototype, "toJSON", VOID);
    }

    public void registerExternalCallback(ExternalCallback cb) {
        externalCallback = cb;
    }

    public void execute(JSONObject ast) throws JSONException, JawascriptRuntimeException {
        evaluate(ast);
    }

    public JSONObject invoke(String funcName, JSONObject input) throws JSONException, JawascriptRuntimeException {
        env = input;
        JawaObjectRef func = global.get(funcName);
        JawaFunc resolvedFunction = (JawaFunc)(func.object);
        HashMap<String, JawaObjectRef> scope = new HashMap<String, JawaObjectRef>();
        LinkedList<HashMap<String, JawaObjectRef>> activation = new LinkedList<HashMap<String, JawaObjectRef>>();
        activation.addLast(scope);
        boolean oldIsFromCallExpression = isFromCallExpression;
        isFromCallExpression = true;
        activations.addLast(activation);
        currentActivation = activation;
        JawaObjectRef ret;
        ret = resolvedFunction.apply();
        activations.removeLast();
        currentActivation = activations.getLast();
        isFromCallExpression = oldIsFromCallExpression;

        JSONObject retJSON = new JSONObject();
        if (ret == null || ret.object == null) {
            retJSON.put("retType", "null");
        } else if (ret.object instanceof JawaArray) {
            retJSON.put("retType", "array");
            retJSON.put("retValue", new JSONArray(((JawaObject)ret.object).toJSON(null).toString()));
        } else if (ret.object instanceof JawaObject) {
            retJSON.put("retType", "object");
            retJSON.put("retValue", new JSONObject(((JawaObject)ret.object).toJSON(null).toString()));
        } else if (ret.object instanceof StringBuilder){
            retJSON.put("retType", "string");
            retJSON.put("retValue", ret.toString());
        } else if (ret.object instanceof Double){
            retJSON.put("retType", "number");
            double value = (Double)ret.object;
            if (Math.abs(Math.round(value) - value) < QUANTUM) {
                retJSON.put("retValue", (long) value);
            } else {
                retJSON.put("retValue", value);
            }
        } else if (ret.object instanceof Boolean) {
            retJSON.put("retType", "boolean");
            retJSON.put("retValue", ret.object.toString());
        }
        return retJSON;
    }
}
