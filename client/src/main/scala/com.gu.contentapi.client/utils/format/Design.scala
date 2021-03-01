package com.gu.contentapi.client.utils.format

sealed trait Design

case object ArticleDesign extends Design
case object MediaDesign extends Design
case object ReviewDesign extends Design
case object AnalysisDesign extends Design
case object CommentDesign extends Design
case object LetterDesign extends Design
case object FeatureDesign extends Design
case object LiveBlogDesign extends Design
case object DeadBlogDesign extends Design
case object RecipeDesign extends Design
case object MatchReportDesign extends Design
case object InterviewDesign extends Design
case object EditorialDesign extends Design
case object QuizDesign extends Design
case object InteractiveDesign extends Design
case object PhotoEssayDesign extends Design
case object PrintShopDesign extends Design
