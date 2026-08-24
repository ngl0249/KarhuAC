package me.liwk.karhu.util.pair;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pair3<X,Y,Z> {

    private X x;
    private Y y;
    private Z z;

}