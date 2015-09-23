package com.nd.android.sdp.dm.options;

import java.io.Serializable;

/**
 * Created by Administrator on 2015/9/23.
 */
public interface TmpFileNameGenerator extends Serializable {

    String generate(String imageUri);

}
