package com.mobdeve.s15.cabinbin.garcia.mco

class DataHelper {
    companion object {
        fun initializeData(): ArrayList<Score> {
            val data = ArrayList<Score>()
            data.add(
                Score(
                    "January 1, 2002", 1200, 1
                )
            )
            data.add(
                Score(
                    "January 1, 2002", 1000, 2
                )
            )
            data.add(
                Score(
                    "January 1, 2002", 800, 3
                )
            )
            data.add(
                Score(
                    "January 1, 2002", 600, 4
                )
            )
            data.add(
                Score(
                    "January 1, 2002", 500, 5
                )
            )
            data.add(
                Score(
                    "January 1, 2002", 400, 6
                )
            )
            data.add(
                Score(
                    "January 1, 2002", 300, 7
                )
            )
            data.add(
                Score(
                    "January 1, 2002", 200, 8
                )
            )
            data.add(
                Score(
                    "January 1, 2002", 100, 9
                )
            )
            data.add(
                Score(
                    "January 1, 2002", 100, 10
                )
            )

            return data;
        }
    }
}