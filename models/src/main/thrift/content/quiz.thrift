struct ResultGroup {
  1: required string title
  2: required string share
  3: required i16 minScore
}

struct Asset {
  1: required string type
  /* what type is this? currently assuming opaque json */
  2: required string data
}

struct Answer {
  1: required string answerText
  2: required list<Asset> assets
  3: required i16 weight
  4: optional string revealText
}

struct ResultBucket {
  1: optional list<Asset> assets
  2: required string description
  3: required string title
  4: required string share
}

struct ResultBuckets {
  1: required list<ResultBucket> buckets
}

struct Question {
  1: required string questionText
  2: required list<Asset> assets
  3: required list<Answer> answers
}

struct ResultGroups {
  1: required list<ResultGroup> groups
}

struct QuizContent {
  1: required list<Question> questions
  2: optional ResultGroups resultGroups
  3: optional ResultBuckets resultBuckets
}

struct QuizAtomData {
  // do we need to store the ID, seeing as it is replicated(?) in the
  // content-atom wrapping?
  1  : required string id
  2  : required string title
  7  : required bool published
  6  : required bool revealAtEnd
  8  : required string quizType
  9  : optional i16 defaultColumns
  10 : required QuizContent content
}
