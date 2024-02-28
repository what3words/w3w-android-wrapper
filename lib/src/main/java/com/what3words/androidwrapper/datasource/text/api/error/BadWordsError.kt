package com.what3words.androidwrapper.datasource.text.api.error

/**
 * This error occurs when the provided words do not form a valid 3 word address, such as "filled.count.soap".
 * The 3 word address must be formatted correctly, with words separated by dots or a Japanese middle dot character (・).
 * Words separated by spaces will be rejected. Optionally, the 3 word address can be prefixed with ///.
 *
 * For more information, see the [what3words convert to coordinates reference docs](https://developer.what3words.com/public-api/docs#convert-to-3wa:~:text=A%203%20word%20address%20as%20a%20string.%20It%20must%20be%20three%20words%20separated%20with%20dots%20or%20a%20japanese%20middle%20dot%20character%20(%E3%83%BB).%20Words%20separated%20by%20spaces%20will%20be%20rejected.%20Optionally%2C%20the%203%20word%20address%20can%20be%20prefixed%20with%20///%20(which%20would%20be%20encoded%20as%20%252F%252F%252F)).
 *
 * @param code The error code associated with the bad words error.
 * @param message A descriptive message providing additional information about the error.
 */
class BadWordsError(code: String, message: String) : W3WApiError(
    code = code,
    errorMessage = message
)