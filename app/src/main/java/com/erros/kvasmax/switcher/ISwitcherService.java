package com.erros.kvasmax.switcher;

/**
 * Created by user on 07.09.2017.
 */

public interface ISwitcherService {

    void onTapIconWithIndex(int position);

    void updateIcons();

    void saveWindowPositions();

}
