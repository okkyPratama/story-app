package com.dicoding.storyapp

import com.dicoding.storyapp.data.StoryItem

object DataDummy {

    fun generateDummyStories(): List<StoryItem> {
        val items: MutableList<StoryItem> = arrayListOf()
        for (i in 0..100) {
            val story = StoryItem(
                id = "story-1lzs6ADeJ_bCHnAs",
                name = "Okky",
                description = "tes",
                photoUrl = "https://story-api.dicoding.dev/images/stories/photos-1683443851252_RSLHFjRt.jpg",
                createdAt = "2023-05-07T07:17:31.255Z",
                lat = -7.00061727791961,
                lon = 110.36580953747034

            )
            items.add(story)
        }
        return items

    }

}