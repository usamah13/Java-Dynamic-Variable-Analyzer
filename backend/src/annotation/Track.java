package annotation;

import java.lang.annotation.Repeatable;

@Repeatable(Tracks.class)
public @interface Track {
    String var();
    String nickname();
}
