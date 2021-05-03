package com.example.maccasfinder.networking




import com.example.maccasfinder.placeResults
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface networkInterface {

    @GET("/maps/api/place/nearbysearch/json")
    fun getId(@Query("key") key : String,
              @Query("location") location : String,
              @Query("name") keyword : String,
              @Query("rankby") rankby : String
    ) : Call<placeResults>


}