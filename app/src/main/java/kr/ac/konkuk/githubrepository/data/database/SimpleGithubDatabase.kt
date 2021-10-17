package kr.ac.konkuk.githubrepository.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import kr.ac.konkuk.githubrepository.data.dao.RepositoryDao
import kr.ac.konkuk.githubrepository.data.entity.GithubRepoEntity

@Database(entities = [GithubRepoEntity::class], version = 1)
abstract class SimpleGithubDatabase : RoomDatabase() {

    abstract fun repositoryDao(): RepositoryDao

}