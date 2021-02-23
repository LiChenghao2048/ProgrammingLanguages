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
        } else if (term.equals("addition")) {
            return this.parse_addition_expression(str, index);
        } else if (term.equals("multiplication")) {
            return this.parse_multiplication_expression(str, index);
        } else if (term.equals("operand")) {
            return this.parse_operand(str, index);
        } else if (term.equals("parenthesis")) {
            return this.parse_parenthesis(str, index);
        } else if (term.equals("space")) {
            return this.parse_space(str, index);
        } else {
            throw new AssertionError("Unexpected term " + term);
        }
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

    private Parse parse_space(String str, int index) {
        while (index < str.length() && str.charAt(index) == ' ') {
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
        test(parser, "3", "integer", new Parse(3, 1));
        test(parser, "0", "integer", new Parse(0, 1));
        test(parser, "100", "integer", new Parse(100, 3));
        test(parser, "2021", "integer", new Parse(2021, 4));
        test(parser, "b", "integer", Parser.FAIL);
        test(parser, "", "integer", Parser.FAIL);
        // addition tests
        test(parser, "b", "addition", Parser.FAIL);
        test(parser, "", "addition", Parser.FAIL);
        test(parser, "3-", "addition", new Parse(3, 1));
        test(parser, "3++", "addition", new Parse(3, 1));
        test(parser, "3+4", "addition", new Parse(7, 3));
        test(parser, "2020+2021", "addition", new Parse(4041, 9));
        test(parser, "0+0", "addition", new Parse(0, 3));
        test(parser, "1+1-", "addition", new Parse(2, 3));
        test(parser, "1+1+-", "addition", new Parse(2, 3));
        test(parser, "0+0+0+0+0", "addition", new Parse(0, 9));
        test(parser, "42+0", "addition", new Parse(42, 4));
        test(parser, "0+42", "addition", new Parse(42, 4));
        test(parser, "123+234+345", "addition", new Parse(702, 11));
        // parenthesis tests
        test(parser, "()", "parenthesis", Parser.FAIL);
        test(parser, "(0)", "parenthesis", new Parse(0, 3));
        test(parser, "(0+0)", "parenthesis", new Parse(0, 5));
        test(parser, "(1+2)", "parenthesis", new Parse(3, 5));
        test(parser, "(1+2+3)", "parenthesis", new Parse(6, 7));
        test(parser, "4+(1+2+3)", "addition", new Parse(10, 9));
        test(parser, "(1+2+3)+5", "addition", new Parse(11, 9));
        test(parser, "4+(1+2+3)+5", "addition", new Parse(15, 11));
        test(parser, "3+4+(5+6)+9", "addition", new Parse(27, 11));
        // end-to-end test
        test(parser, "(3+4)+((2+3)+0+(1+2+3))+9", "addition", new Parse(27, 25));
        test(parser, "1+1+b", "addition", new Parse(2, 3));

        // space tests
        test(parser, " ", "space", Parser.FAIL);
        test(parser, "   ", "space", Parser.FAIL);
        test(parser, "", "space", Parser.FAIL);
        test(parser, "3 ", "integer", new Parse(3, 1));
        test(parser, "  3 ", "integer", Parser.FAIL);
        test(parser, "1+ 2", "addition", new Parse(3, 4));
        test(parser, "3 + 4 ", "addition", new Parse(7, 5));
        test(parser, "3 + 4   +     ", "addition", new Parse(7, 5));
        test(parser, "5     +    6", "addition", new Parse(11, 12));
        test(parser, "5   +  6   ", "addition", new Parse(11, 8));
        test(parser, "(  5 )", "parenthesis", new Parse(5, 6));
        test(parser, "( )", "parenthesis", Parser.FAIL);
        test(parser, "(  1  + 2+   3    )", "addition", new Parse(6, 19));
        test(parser, "( 3 +4)  + (( 2   +3 )+ 0+( 1+2+  3))+ 9  ", "addition", new Parse(27, 40));

        // add or sub
        test(parser, "1+1-1", "addition", new Parse(1, 5));
        test(parser, "1-1+1", "addition", new Parse(1, 5));
        test(parser, "( 3 +4)  - (( 2   +3 )- 0+( 1-2+  3))+ 9  ", "addition", new Parse(9, 40));

        // mul or div
        test(parser, "3*4", "multiplication", new Parse(12, 3));
        test(parser, "8 /    2", "multiplication", new Parse(4, 8));
        test(parser, "(  1  + 2*   3    )", "addition", new Parse(7, 19));
        test(parser, "(  (1  + 2) /   3    )", "addition", new Parse(1, 22));
        test(parser, "( 3 *4)  - (( 2   +3 ) /5- 0+( 1-2+  3))+ 9  ", "addition", new Parse(18, 43));
        test(parser, "3+*", "addition", new Parse(3, 1));
        test(parser, "3+3*", "addition", new Parse(6, 3));

    }

    public static void main(String[] args) {
        test();
    }

}