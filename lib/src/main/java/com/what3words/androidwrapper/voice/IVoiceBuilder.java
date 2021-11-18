package com.what3words.androidwrapper.voice;

import androidx.core.util.Consumer;

import com.what3words.javawrapper.request.BoundingBox;
import com.what3words.javawrapper.request.Coordinates;
import com.what3words.javawrapper.response.APIResponse;
import com.what3words.javawrapper.response.Suggestion;

import java.util.List;

public interface IVoiceBuilder {
    /**
     * onSuggestions callback will be called when VoiceAPI returns a set of suggestion after
     * receiving the voice data, this can be empty in case of no suggestions available for the provided voice record.
     *
     * @param callback with a list of {@link Suggestion} returned by our VoiceAPI
     * @return a {@link VoiceBuilder} instance
     */
    IVoiceBuilder onSuggestions(Consumer<List<Suggestion>> callback);

    /**
     * onError callback will be called when some API error occurs on the VoiceAPI
     *
     * @param callback will be called when an {@link APIResponse.What3WordsError} occurs
     * @return a {@link VoiceBuilder} instance
     */
    IVoiceBuilder onError(Consumer<APIResponse.What3WordsError> callback);

    /**
     * startListening() starts the {@link Microphone} recording and starts sending voice data to our VoiceAPI.
     *
     * @return a {@link VoiceBuilder} instance
     */
    IVoiceBuilder startListening();

    /**
     * isListening() can be used to check if is currently in recording state.
     *
     * @return a {@link VoiceBuilder} instance
     */
    Boolean isListening();

    /**
     * stopListening() forces the {@link Microphone} to stop recording and closes the socket with our VoiceAPI.
     */
    void stopListening();

    /**
     * This is a location, specified as a latitude (often where the user making the query is). If specified, the results will be weighted to
     * give preference to those near the <code>focus</code>. For convenience, longitude is allowed to wrap around the 180 line, so 361 is equivalent to 1.
     *
     * @param coordinates the focus to use
     * @return a {@link IVoiceBuilder} instance
     */
    IVoiceBuilder focus(Coordinates coordinates);

    /**
     * Set the number of AutoSuggest results to return. A maximum of 100 results can be specified, if a number greater than this is requested,
     * this will be truncated to the maximum. The default is 3
     *
     * @param n the number of AutoSuggest results to return, or null to set back to default
     * @return a {@link VoiceBuilder} instance
     */
    IVoiceBuilder nResults(Integer n);

    /**
     * Specifies the number of results (must be &lt;= nResults) within the results set which will have a focus. Defaults to <code>nResults</code>.
     * This allows you to run autosuggest with a mix of focussed and unfocussed results, to give you a "blend" of the two. This is exactly what the old V2
     * <code>standardblend</code> did, and <code>standardblend</code> behaviour can easily be replicated by passing <code>nFocusResults=1</code>,
     * which will return just one focussed result and the rest unfocussed.
     *
     * @param n number of results within the results set which will have a focus, or set to null to clear filter
     * @return a {@link VoiceBuilder} instance
     */
    IVoiceBuilder nFocusResults(Integer n);

    /**
     * Restrict autosuggest results to a circle, specified by <code>Coordinates</code> representing the centre of the circle, plus the
     * <code>radius</code> in kilometres. For convenience, longitude is allowed to wrap around 180 degrees. For example 181 is equivalent to -179.
     *
     * @param centre the centre of the circle
     * @param radius the radius of the circle in kilometres
     * @return a {@link VoiceBuilder} instance
     */
    IVoiceBuilder clipToCircle(
            Coordinates centre,
            Double radius
    );

    /**
     * Restricts autosuggest to only return results inside the countries specified by comma-separated list of uppercase ISO 3166-1 alpha-2 country codes
     * (for example, to restrict to Belgium and the UK, use <code>clipToCountry("GB", "BE")</code>. <code>clipToCountry</code> will also accept lowercase
     * country codes. Entries must be two a-z letters. WARNING: If the two-letter code does not correspond to a country, there is no error: API simply
     * returns no results.
     *
     * @param countryCodes countries to clip results too
     * @return a {@link VoiceBuilder} instance
     */
    IVoiceBuilder clipToCountry(List<String> countryCodes);

    /**
     * Restrict autosuggest results to a <code>BoundingBox</code>.
     *
     * @param boundingBox <code>BoundingBox</code> to clip results too
     * @return a {@link VoiceBuilder} instance
     */
    IVoiceBuilder clipToBoundingBox(
            BoundingBox boundingBox
    );

    /**
     * Restrict autosuggest results to a polygon, specified by a collection of <code>Coordinates</code>. The polygon should be closed,
     * i.e. the first element should be repeated as the last element; also the list should contain at least 4 entries. The API is currently limited to
     * accepting up to 25 pairs.
     *
     * @param polygon the polygon to clip results too
     * @return a {@link VoiceBuilder} instance
     */
    IVoiceBuilder clipToPolygon(
            List<Coordinates> polygon
    );
}
