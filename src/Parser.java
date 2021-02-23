public class Parser {

    static Parse FAIL = new Parse(0, -1);

    public Parse parse(String str, String term) {
        return this.parse(str, 0, term);
    }

    private Parse parse(String str, int index, String term) {
        if (index >= str.length()) {
            return Parser.FAIL;
        }
        if (term.equals("integer")) {
            return this.parse_integer(str, index);
        } else if (term.equals("expression")) {
            return this.parse_expression(str, index);
        } else if (term.equals("statement")) {
            return this.parse_statement(str, index);
        } else if (term.equals("addition")) {
            return this.parse_addition_expression(str, index);
        } else if (term.equals("multiplication")) {
            return this.parse_multiplication_expression(str, index);
        } else if (term.equals("operand")) {
            return this.parse_operand(str, index);
        } else if (term.equals("parenthesis")) {
            return this.parse_parenthesis(str, index);
        } else if (term.equals("space")) {
            return this.parse_opt_space(str, index);
        } else if (term.equals("req_space")) {
            return this.parse_req_space(str, index);
        } else if (term.equals("print")) {
            return this.parse_print_statement(str, index);
        } else {
            throw new AssertionError("Unexpected term " + term);
        }
    }

    private Parse parse_statement(String str, int index) {
        Parse parse = parse_print_statement(str, index);
        if (parse.equals(Parser.FAIL)) {
            parse = parse_expression(str, index);
        }
        return parse;
    }

    private Parse parse_print_statement(String str, int index) {

        if (str.charAt(index) != 'p') {return Parser.FAIL;}
        if (str.charAt(index+1) != 'r') {return Parser.FAIL;}
        if (str.charAt(index+2) != 'i') {return Parser.FAIL;}
        if (str.charAt(index+3) != 'n') {return Parser.FAIL;}
        if (str.charAt(index+4) != 't') {return Parser.FAIL;}

        Parse parse = parse_req_space(str, index + 5);
        if (parse.equals(Parser.FAIL)) {
            return parse;
        }
        parse = parse_expression(str, parse.getIndex());
        if (parse.equals(Parser.FAIL)) {
            return parse;
        }
        int result = parse.getValue();
        parse = parse_opt_space(str, parse.getIndex());
        if (parse.equals(Parser.FAIL)) {
            return parse;
        }
        if (parse.getIndex() == str.length()-1 && str.charAt(parse.getIndex()) == ';') {
            System.out.println(result);
            return new Parse(result, parse.getIndex()+1);
        }
        return Parser.FAIL;
    }

    private Parse parse_req_space(String str, int index) {
        if (str.charAt(index) != ' ') {return Parser.FAIL;}
        return parse_opt_space(str, index);
    }

    private Parse parse_expression(String str, int index) {
        Parse parse = parse_addition_expression(str, index);
        return parse;
    }

    private Parse parse_multiplication_expression(String str, int index) {
        Parse parse = this.parse(str, index,"operand");
        int result = 0;
        if (parse.equals(Parser.FAIL)) {
            return Parser.FAIL;
        }
        result = parse.getValue();
        index = parse.getIndex();
        while (index < str.length() && !parse.equals(Parser.FAIL)) {

            parse = this.parse(str, parse.getIndex(), "space");
            if (parse.equals(Parser.FAIL)) {break;}

            if (str.charAt(parse.getIndex()) != '*' && str.charAt(parse.getIndex()) != '/') {
                parse = Parser.FAIL;
                break;
            }
            Character mul_div_operator = str.charAt(parse.getIndex());

            parse = this.parse(str, parse.getIndex() + 1, "space");
            if (parse.equals(Parser.FAIL)) {break;}

            parse = this.parse(str, parse.getIndex(), "operand");
            if (parse.equals(Parser.FAIL)) {
                parse = Parser.FAIL;
                break;
            }

            if (mul_div_operator == '*') {
                result *= parse.getValue();
            } else if (mul_div_operator == '/') {
                result /= parse.getValue();
            }
            index = parse.getIndex();
        }
        return new Parse(result, index);
    }

    private Parse parse_opt_space(String str, int index) {
        while (index < str.length() && (str.charAt(index) == ' ')) {
            index += 1;
        }

        if (index < str.length()) {
            return new Parse(0, index);
        } else {
            return Parser.FAIL;
        }
    }

    private Parse parse_operand(String str, int index) {
        Parse parse = this.parse(str, index, "integer");
        if (!parse.equals(Parser.FAIL)) {
            return parse;
        }
        parse = this.parse(str, index, "parenthesis");
        if (!parse.equals(Parser.FAIL)) {
            return parse;
        }
        return Parser.FAIL;
    }

    private Parse parse_parenthesis(String str, int index) {
        if (str.charAt(index) != '(') {
            return Parser.FAIL;
        }

        Parse parse = this.parse(str, index + 1, "space");
        if (parse.equals(Parser.FAIL)) {return Parser.FAIL;}

        parse =  this.parse(str, parse.getIndex(), "addition");
        if (parse.equals(Parser.FAIL)) {
            return Parser.FAIL;
        }
        int result = parse.getValue();

        parse = this.parse(str, parse.getIndex(), "space");
        if (parse.equals(Parser.FAIL)) {return Parser.FAIL;}

        if (str.charAt(parse.getIndex()) != ')') {
            return Parser.FAIL;
        }
        return new Parse(result, parse.getIndex() + 1);
    }

    private Parse parse_addition_expression(String str, int index) {
        Parse parse = this.parse(str, index,"multiplication");
        int result = 0;
        if (parse.equals(Parser.FAIL)) {
            return Parser.FAIL;
        }
        result = parse.getValue();
        index = parse.getIndex();
        while (index < str.length() && !parse.equals(Parser.FAIL)) {

            parse = this.parse(str, parse.getIndex(), "space");
            if (parse.equals(Parser.FAIL)) {break;}

            if (str.charAt(parse.getIndex()) != '+' && str.charAt(parse.getIndex()) != '-') {
                parse = Parser.FAIL;
                break;
            }
            Character add_sub_operator = str.charAt(parse.getIndex());

            parse = this.parse(str, parse.getIndex() + 1, "space");
            if (parse.equals(Parser.FAIL)) {break;}

            parse = this.parse(str, parse.getIndex(), "multiplication");
            if (parse.equals(Parser.FAIL)) {
                parse = Parser.FAIL;
                break;
            }

            if (add_sub_operator == '+') {
                result += parse.getValue();
            } else if (add_sub_operator == '-') {
                result -= parse.getValue();
            }
            index = parse.getIndex();
        }
        return new Parse(result, index);
    }

    private Parse parse_integer(String str, int index) {
        String parsed = "";
        while (index < str.length() &&
                Character.isDigit(str.charAt(index))) {
            parsed += str.charAt(index);
            index++;
        }
        if (parsed.equals("")) {
            return Parser.FAIL;
        }
        return new Parse(Integer.parseInt(parsed), index);
    }

    private static void test(Parser parser, String str, String term, Parse expected) {
        Parse actual = parser.parse(str, term);
        if (actual == null) {
            throw new AssertionError("Got null when parsing \"" + str + "\"");
        }
        if (!actual.equals(expected)) {
            throw new AssertionError("Parsing \"" + str + "\"; expected " + expected + " but got " + actual);
        }
    }

    public static void test() {
        Parser parser = new Parser();
        // integer tests
        test(parser, "3", "expression", new Parse(3, 1));
        test(parser, "0", "expression", new Parse(0, 1));
        test(parser, "100", "expression", new Parse(100, 3));
        test(parser, "2021", "expression", new Parse(2021, 4));
        test(parser, "b", "expression", Parser.FAIL);
        test(parser, "", "expression", Parser.FAIL);
        // addition tests
        test(parser, "b", "expression", Parser.FAIL);
        test(parser, "", "expression", Parser.FAIL);
        test(parser, "3-", "expression", new Parse(3, 1));
        test(parser, "3++", "expression", new Parse(3, 1));
        test(parser, "3+4", "expression", new Parse(7, 3));
        test(parser, "2020+2021", "expression", new Parse(4041, 9));
        test(parser, "0+0", "expression", new Parse(0, 3));
        test(parser, "1+1-", "expression", new Parse(2, 3));
        test(parser, "1+1+-", "expression", new Parse(2, 3));
        test(parser, "0+0+0+0+0", "expression", new Parse(0, 9));
        test(parser, "42+0", "expression", new Parse(42, 4));
        test(parser, "0+42", "expression", new Parse(42, 4));
        test(parser, "123+234+345", "expression", new Parse(702, 11));
        // parenthesis tests
        test(parser, "()", "expression", Parser.FAIL);
        test(parser, "(0)", "expression", new Parse(0, 3));
        test(parser, "(0+0)", "expression", new Parse(0, 5));
        test(parser, "(1+2)", "expression", new Parse(3, 5));
        test(parser, "(1+2+3)", "expression", new Parse(6, 7));
        test(parser, "4+(1+2+3)", "expression", new Parse(10, 9));
        test(parser, "(1+2+3)+5", "expression", new Parse(11, 9));
        test(parser, "4+(1+2+3)+5", "expression", new Parse(15, 11));
        test(parser, "3+4+(5+6)+9", "expression", new Parse(27, 11));
        // end-to-end tests
        test(parser, "(3+4)+((2+3)+0+(1+2+3))+9", "expression", new Parse(27, 25));
        test(parser, "1+1+b", "expression", new Parse(2, 3));

        // space tests
        test(parser, " ", "expression", Parser.FAIL);
        test(parser, "   ", "expression", Parser.FAIL);
        test(parser, "", "expression", Parser.FAIL);
        test(parser, "3 ", "expression", new Parse(3, 1));
        test(parser, "  3 ", "expression", Parser.FAIL);
        test(parser, "1+ 2", "expression", new Parse(3, 4));
        test(parser, "3 + 4 ", "expression", new Parse(7, 5));
        test(parser, "3 + 4   +     ", "expression", new Parse(7, 5));
        test(parser, "5     +    6", "expression", new Parse(11, 12));
        test(parser, "5   +  6   ", "expression", new Parse(11, 8));
        test(parser, "(  5 )", "expression", new Parse(5, 6));
        test(parser, "( )", "expression", Parser.FAIL);
        test(parser, "(  1  + 2+   3    )", "expression", new Parse(6, 19));
        test(parser, "( 3 +4)  + (( 2   +3 )+ 0+( 1+2+  3))+ 9  ", "expression", new Parse(27, 40));

        // add or sub tests
        test(parser, "1+1-1", "expression", new Parse(1, 5));
        test(parser, "1-1+1", "expression", new Parse(1, 5));
        test(parser, "( 3 +4)  - (( 2   +3 )- 0+( 1-2+  3))+ 9  ", "expression", new Parse(9, 40));

        // mul or div tests
        test(parser, "3*4", "expression", new Parse(12, 3));
        test(parser, "8 /    2", "expression", new Parse(4, 8));
        test(parser, "(  1  + 2*   3    )", "expression", new Parse(7, 19));
        test(parser, "(  (1  + 2) /   3    )", "expression", new Parse(1, 22));
        test(parser, "( 3 *4)  - (( 2   +3 ) /5- 0+( 1-2+  3))+ 9  ", "expression", new Parse(18, 43));
        test(parser, "3+*", "expression", new Parse(3, 1));
        test(parser, "3+3*", "expression", new Parse(6, 3));

        // req_space tests
        test(parser, "", "req_space", Parser.FAIL);
        test(parser, " ", "req_space", Parser.FAIL);
        test(parser, "  ", "req_space", Parser.FAIL);
        test(parser, "1", "req_space", Parser.FAIL);
        test(parser, " 1", "req_space", new Parse(0, 1));
        test(parser, "  1", "req_space", new Parse(0, 2));

        // print statement tests
        test(parser, "print 2+3;", "print", new Parse(5, 10));
        test(parser, "print     2 + 3    ;", "print", new Parse(5, 20));
        test(parser, "print 2+3", "print", Parser.FAIL);
        test(parser, "print 2*3;", "print", new Parse(6, 10));
        test(parser, "print  ( 3 *4)  - (( 2   +3 ) /5- 0+( 1-2+  3))+ 9  ;", "print", new Parse(18, 53));
        test(parser, " 2+3", "print", Parser.FAIL);
        test(parser, "2+3", "print", Parser.FAIL);

        // statement tests
        test(parser, "3", "statement", new Parse(3, 1));
        test(parser, "0", "statement", new Parse(0, 1));
        test(parser, "100", "statement", new Parse(100, 3));
        test(parser, "2021", "statement", new Parse(2021, 4));
        test(parser, "b", "statement", Parser.FAIL);
        test(parser, "", "statement", Parser.FAIL);
        test(parser, "3*4", "statement", new Parse(12, 3));
        test(parser, "8 /    2", "statement", new Parse(4, 8));
        test(parser, "(  1  + 2*   3    )", "statement", new Parse(7, 19));
        test(parser, "(  (1  + 2) /   3    )", "statement", new Parse(1, 22));
        test(parser, "( 3 *4)  - (( 2   +3 ) /5- 0+( 1-2+  3))+ 9  ", "statement", new Parse(18, 43));
        test(parser, "3+*", "statement", new Parse(3, 1));
        test(parser, "3+3*", "statement", new Parse(6, 3));
        test(parser, "print 2+3;", "statement", new Parse(5, 10));
        test(parser, "print     2 + 3    ;", "statement", new Parse(5, 20));
        test(parser, "print 2+3", "statement", Parser.FAIL);
        test(parser, "print 2*3;", "statement", new Parse(6, 10));
        test(parser, "print  ( 3 *4)  - (( 2   +3 ) /5- 0+( 1-2+  3))+ 9  ;", "statement", new Parse(18, 53));
        test(parser, " 2+3", "statement", Parser.FAIL);
        test(parser, "2+3", "statement", new Parse(5, 3));
    }

    public static void main(String[] args) {
        test();
    }

}