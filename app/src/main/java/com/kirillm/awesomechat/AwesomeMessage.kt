package com.kirillm.awesomechat

import android.icu.lang.UCharacter.GraphemeClusterBreak.T

data class AwesomeMessage (
    var text: String = "",
    var name: String = "",
    var imageUrl: String = "",
    var sender: String = "",
    var recipient: String = "",
    var isMine: Boolean = true
)