package me.redstoyn33.signmod;

import java.util.Arrays;

public class SignCode {
    public static String[] codes = new String[]{
            "1" // encode stable +pos
            , "2" // uncode stable +pos
            , "3" // encode compress +pos
            , "4" // uncode compress +pos
            , "5" // encode stable -pos
            , "6" // uncode stable -pos
            , "7" // encode compress -pos
            , "8" // uncode compress -pos
    };
    public boolean encode;
    public boolean stable;
    public boolean pos;

    public static SignCode newCode(String s) {
        if (Arrays.stream(codes).noneMatch(s1 -> s1.equals(s))) return null;
        SignCode c = new SignCode();
        int i;
        for (i = 0; i < codes.length; i++) {
            if (s.equals(codes[i])) break;
        }
        c.encode = i % 2 == 0;
        c.stable = (i / 2) % 2 == 0;
        c.pos = (i / 4) % 2 == 0;
        return c;
    }
    public static String newCode(boolean encode,boolean stable, boolean pos){
        return codes[(encode?0:1) +(stable?0:2) +(pos?0:4)];
    }
}
