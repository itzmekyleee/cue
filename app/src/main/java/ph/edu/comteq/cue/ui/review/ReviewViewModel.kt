package ph.edu.comteq.cue.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ph.edu.comteq.cue.data.domain.review.ReviewBoxes
import ph.edu.comteq.cue.data.local.entity.Card
import ph.edu.comteq.cue.data.repository.CardRepository
import ph.edu.comteq.cue.data.repository.ReviewRepository
class ReviewViewModel(
    private val subjectId: Long?,
    private val cardRepository: CardRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {
    private val _queue = MutableStateFlow<List<Card>>(emptyList())
    val queue: StateFlow<List<Card>> = _queue

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex

    private val _isFlipped = MutableStateFlow(false)
    val isFlipped: StateFlow<Boolean> = _isFlipped

    private val _sessionCorrect = MutableStateFlow(0)
    val sessionCorrect: StateFlow<Int> = _sessionCorrect

    private val _sessionTotal = MutableStateFlow(0)
    val sessionTotal: StateFlow<Int> = _sessionTotal

    val currentCard: StateFlow<Card?> = combine(_queue, _currentIndex) { cards, index ->
        cards.getOrNull(index)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val isSessionComplete: StateFlow<Boolean> = combine(_queue, _currentIndex) { cards, index ->
        cards.isNotEmpty() && index >= cards.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    init {
        loadDueCards()
    }

    private fun loadDueCards() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val due = if (subjectId != null) {
                cardRepository.getDueCardsForSubject(subjectId, now)
            } else {
                cardRepository.getDueCards(now)
            }
            _queue.value = ReviewBoxes.orderForSession(due)
        }
    }

    fun flipCard() {
        _isFlipped.value = true
    }

    fun answer(wasCorrect: Boolean) {
        val card = currentCard.value ?: return
        viewModelScope.launch {
            val (newBox, newDate) = ReviewBoxes.nextBoxAndDate(card.box, wasCorrect)
            cardRepository.update(card.copy(box = newBox, nextReviewDate = newDate))
            reviewRepository.log(card.id, wasCorrect)

            _sessionTotal.value += 1
            if (wasCorrect) _sessionCorrect.value += 1

            _isFlipped.value = false
            _currentIndex.value += 1
        }
    }
}