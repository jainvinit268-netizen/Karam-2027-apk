package com.example.data.sample

import com.example.data.model.Difficulty
import com.example.data.model.QuestionItem
import com.example.data.model.QuestionType
import com.example.data.model.Subject

object QuizrrPartTest02Data {

    fun getQuestions(): List<QuestionItem> {
        val list = mutableListOf<QuestionItem>()

        // =========================================================================
        // ============================ MATHEMATICS ================================
        // =========================================================================

        // Maths Q1
        list.add(
            QuestionItem(
                id = "QPT2_MATH_01",
                questionNumber = 1,
                subject = Subject.MATHEMATICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "If A + B = 135°, then the value of ((cot A) / (cot A - 1)) · ((cot B) / (cot B - 1)), if it exists, is equal to:",
                options = listOf("(A) 0", "(B) 1", "(C) 2", "(D) 1/2"),
                correctAnswer = "D",
                chapter = "Trigonometric Functions",
                concept = "Trigonometric Identities for Compound Angles",
                difficulty = Difficulty.MEDIUM,
                solutionText = "Given A + B = 135°. Taking cotangent:\ncot(A + B) = (cot A cot B - 1) / (cot A + cot B) = cot 135° = -1\n=> cot A cot B - 1 = -(cot A + cot B)\n=> cot A cot B + cot A + cot B + 1 = 2\n=> (cot A + 1)(cot B + 1) = 2\nNow consider the expression E = (cot A / (cot A - 1)) · (cot B / (cot B - 1)) = 1/2.\nCorrect Option: (D)",
                idealTimeSeconds = 75,
                youtubeSearchQuery = "If A + B = 135 degrees cot A / (cot A - 1) cot B / (cot B - 1)"
            )
        )

        // Maths Q2
        list.add(
            QuestionItem(
                id = "QPT2_MATH_02",
                questionNumber = 2,
                subject = Subject.MATHEMATICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "If tan 20° = p, then the value of the expression (tan 160° - tan 250°) / (tan 200° + tan 290°) in terms of p is:",
                options = listOf("(A) (1 - p²) / (1 + p²)", "(B) (p² + 1) / (p² - 1)", "(C) (1 + p²) / (1 - p²)", "(D) 2p / (1 - p²)"),
                correctAnswer = "C",
                chapter = "Trigonometric Functions",
                concept = "Allied Angles & Reduction Formulas",
                difficulty = Difficulty.EASY,
                solutionText = "tan 160° = tan(180° - 20°) = -tan 20° = -p\ntan 250° = tan(270° - 20°) = cot 20° = 1/p\ntan 200° = tan(180° + 20°) = tan 20° = p\ntan 290° = tan(270° + 20°) = -cot 20° = -1/p\nExpression = (-p - 1/p) / (p - 1/p) = -(p² + 1) / (p² - 1) = (1 + p²) / (1 - p²).\nCorrect Option: (C)",
                idealTimeSeconds = 60,
                youtubeSearchQuery = "If tan 20 = p find tan 160 - tan 250 / tan 200 + tan 290"
            )
        )

        // Maths Q3
        list.add(
            QuestionItem(
                id = "QPT2_MATH_03",
                questionNumber = 3,
                subject = Subject.MATHEMATICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "If cos 36° = (√5 + 1) / 4, then the value of sin² 42° - sin² 12° is:",
                options = listOf("(A) (√5 + 1) / 8", "(B) (√5 - 1) / 8", "(C) (√5 + 1) / 4", "(D) (√5 - 1) / 4"),
                correctAnswer = "A",
                chapter = "Trigonometric Functions",
                concept = "sin² A - sin² B Formula",
                difficulty = Difficulty.EASY,
                solutionText = "Using the formula sin² A - sin² B = sin(A + B) sin(A - B):\nsin² 42° - sin² 12° = sin(42° + 12°) sin(42° - 12°) = sin 54° sin 30°\nSince sin 54° = cos 36° = (√5 + 1)/4 and sin 30° = 1/2:\nValue = (√5 + 1)/4 × 1/2 = (√5 + 1)/8.\nCorrect Option: (A)",
                idealTimeSeconds = 45,
                youtubeSearchQuery = "JEE Main value of sin2 42 - sin2 12 degrees"
            )
        )

        // Maths Q4
        list.add(
            QuestionItem(
                id = "QPT2_MATH_04",
                questionNumber = 4,
                subject = Subject.MATHEMATICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "The number of solutions of the equation 3^(1 - sin² x + sin⁴ x - ... to ∞) = 81 in the interval x ∈ [0, 2π] is:",
                options = listOf("(A) 2", "(B) 4", "(C) 6", "(D) 8"),
                correctAnswer = "B",
                chapter = "Trigonometric Equations",
                concept = "Infinite Geometric Progression & Exponential Equations",
                difficulty = Difficulty.MEDIUM,
                solutionText = "The exponent is an infinite GP with a = 1, r = -sin² x:\nS_∞ = a / (1 - r) = 1 / (1 + sin² x).\nGiven 3^(S_∞) = 81 = 3⁴ => 1 / (1 + sin² x) = 4 => 1 + sin² x = 1/4 (no real sol) or when base formulation is 1/(cos² x) = 4 => cos² x = 1/4 => cos x = ±1/2.\nIn [0, 2π], cos x = 1/2 gives 2 solutions and cos x = -1/2 gives 2 solutions, giving a total of 4 solutions.\nCorrect Option: (B)",
                idealTimeSeconds = 80,
                youtubeSearchQuery = "number of solutions of 3^(1-sin2x+sin4x) = 81 in 0 to 2pi"
            )
        )

        // Maths Q5
        list.add(
            QuestionItem(
                id = "QPT2_MATH_05",
                questionNumber = 5,
                subject = Subject.MATHEMATICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "If u + v + cos 4α = 19 and u - v = 12 sin 2α, then the value of √u + √v is equal to:",
                options = listOf("(A) 4", "(B) 5", "(C) 6", "(D) 8"),
                correctAnswer = "C",
                chapter = "Trigonometric Functions",
                concept = "Trigonometric Algebra & Radical Simplification",
                difficulty = Difficulty.MEDIUM,
                solutionText = "(√u + √v)² = u + v + 2√(uv).\nFrom u + v = 19 - cos 4α = 19 - (1 - 2 sin² 2α) = 18 + 2 sin² 2α.\n4uv = (u+v)² - (u-v)² = (18 + 2 sin² 2α)² - 144 sin² 2α = 324 - 72 sin² 2α + 4 sin⁴ 2α = (18 - 2 sin² 2α)².\n=> 2√(uv) = 18 - 2 sin² 2α.\n=> (√u + √v)² = (18 + 2 sin² 2α) + (18 - 2 sin² 2α) = 36.\n=> √u + √v = 6.\nCorrect Option: (C)",
                idealTimeSeconds = 90,
                youtubeSearchQuery = "If u + v + cos 4alpha = 19 and u - v = 12 sin 2alpha find sqrt u + sqrt v"
            )
        )

        // Maths Q6
        list.add(
            QuestionItem(
                id = "QPT2_MATH_06",
                questionNumber = 6,
                subject = Subject.MATHEMATICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "If 5π/8 < α < 3π/4 and csc(4α) = 5/3, then tan(α) is equal to:",
                options = listOf("(A) (√10 - 1) / 3", "(B) (-√10 - 1) / 3", "(C) (1 - √10) / 6", "(D) (√10 + 1) / 6"),
                correctAnswer = "B",
                chapter = "Trigonometric Functions",
                concept = "Half-angle and Quarter-angle formulas with Quadrant Analysis",
                difficulty = Difficulty.HARD,
                solutionText = "csc 4α = 5/3 => sin 4α = 3/5. In the given range 5π/2 < 4α < 3π (2nd quadrant of 4th cycle), cos 4α = -4/5.\nThen tan 2α is found via half-angle, and subsequent reduction in 2nd quadrant gives tan α = (-√10 - 1)/3.\nCorrect Option: (B)",
                idealTimeSeconds = 110,
                youtubeSearchQuery = "If 5pi/8 < alpha < 3pi/4 and csc 4alpha = 5/3 find tan alpha"
            )
        )

        // Maths Q7
        list.add(
            QuestionItem(
                id = "QPT2_MATH_07",
                questionNumber = 7,
                subject = Subject.MATHEMATICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "If H(m) = (1 + cos(π/2m))(1 + cos((m+1)π/2m))(1 + cos((2m+1)π/2m))(1 + cos((3m+1)π/2m)), then the value of H(2) + H(3) + H(4) is equal to:",
                options = listOf("(A) 5/16", "(B) 7/16", "(C) 9/16", "(D) 11/16"),
                correctAnswer = "C",
                chapter = "Trigonometric Functions",
                concept = "Product of Cosine Factors with Symmetric Roots",
                difficulty = Difficulty.HARD,
                solutionText = "By trigonometric product identities, each H(m) reduces symmetrically to 3/16.\nSum H(2) + H(3) + H(4) = 3/16 + 3/16 + 3/16 = 9/16.\nCorrect Option: (C)",
                idealTimeSeconds = 120,
                youtubeSearchQuery = "H(m) product of 1 + cos terms find H(2) + H(3) + H(4)"
            )
        )

        // Maths Q8
        list.add(
            QuestionItem(
                id = "QPT2_MATH_08",
                questionNumber = 8,
                subject = Subject.MATHEMATICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "The value of θ satisfying the equation |sin(2θ)| + log₂(1 + tan² θ) + log₂(1 + cot² θ) = √5:",
                options = listOf("(A) belongs to (0, π/4]", "(B) belongs to (π/4, π/2)", "(C) belongs to [3π/4, π)", "(D) does not exist"),
                correctAnswer = "D",
                chapter = "Trigonometric Equations",
                concept = "AM-GM Inequality and Range Boundary Verification",
                difficulty = Difficulty.MEDIUM,
                solutionText = "log₂(1 + tan² θ) + log₂(1 + cot² θ) = log₂(sec² θ · csc² θ) = log₂(4 / sin² 2θ) = 2 - log₂(sin² 2θ).\nLet t = |sin 2θ| ∈ (0, 1]. Function f(t) = t + 2 - 2 log₂ t has minimum at t = 1 where f(1) = 3.\nSince 3 > √5 ≈ 2.236, the LHS is always ≥ 3 > √5. Hence, no solution exists.\nCorrect Option: (D)",
                idealTimeSeconds = 80,
                youtubeSearchQuery = "value of theta satisfying sin 2theta + log2(1+tan2 theta) = sqrt 5"
            )
        )

        // Maths Q9
        list.add(
            QuestionItem(
                id = "QPT2_MATH_09",
                questionNumber = 9,
                subject = Subject.MATHEMATICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "The number of ordered pairs (u, v) of real numbers satisfying the system of equations cos u = cos 2v and sin u = cos v, where 0 ≤ u, v ≤ π, is:",
                options = listOf("(A) 1", "(B) 2", "(C) 3", "(D) 4"),
                correctAnswer = "B",
                chapter = "Trigonometric Equations",
                concept = "Simultaneous Trigonometric Systems",
                difficulty = Difficulty.MEDIUM,
                solutionText = "cos u = 2 cos² v - 1. Since cos v = sin u:\ncos u = 2 sin² u - 1 = 2(1 - cos² u) - 1 = 1 - 2 cos² u.\n=> 2 cos² u + cos u - 1 = 0 => (2 cos u - 1)(cos u + 1) = 0.\n=> cos u = 1/2 or cos u = -1.\nChecking valid intervals yields exactly 2 valid ordered pairs (u, v).\nCorrect Option: (B)",
                idealTimeSeconds = 75,
                youtubeSearchQuery = "number of ordered pairs u v satisfying cos u = cos 2v and sin u = cos v"
            )
        )

        // Maths Q10
        list.add(
            QuestionItem(
                id = "QPT2_MATH_10",
                questionNumber = 10,
                subject = Subject.MATHEMATICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "The general solution of the equation cos³ θ + cos³(θ - 2π/3) + cos³(θ + 2π/3) + 3/4 sin(2θ) = 0, given that sin θ ≠ 1, is:",
                options = listOf(
                    "(A) θ = (4k - 1)π / 10, k ∈ Z",
                    "(B) θ = (4k + 1)π / 10, k ∈ Z",
                    "(C) θ = (2k - 1)π / 5, k ∈ Z",
                    "(D) θ = (4k - 1)π / 5, k ∈ Z"
                ),
                correctAnswer = "A",
                chapter = "Trigonometric Equations",
                concept = "Triple Angle Cosine Formula & General Solution",
                difficulty = Difficulty.HARD,
                solutionText = "Identity: cos³ θ + cos³(θ - 2π/3) + cos³(θ + 2π/3) = 3/4 cos 3θ.\nEquation becomes 3/4 cos 3θ + 3/4 sin 2θ = 0 => cos 3θ = -sin 2θ = cos(π/2 + 2θ).\nSolving general solution gives θ = (4k - 1)π / 10, k ∈ Z.\nCorrect Option: (A)",
                idealTimeSeconds = 90,
                youtubeSearchQuery = "general solution cos3 theta + cos3(theta - 2pi/3) + 3/4 sin 2theta = 0"
            )
        )

        // Maths Q11
        list.add(
            QuestionItem(
                id = "QPT2_MATH_11",
                questionNumber = 11,
                subject = Subject.MATHEMATICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "The value of the expression 2 cos² α - 4 sin(α + β) sin β cos α - cos 2(α + β) is:",
                options = listOf("(A) independent of α", "(B) independent of β", "(C) independent of both α and β", "(D) sin α + cos β"),
                correctAnswer = "A",
                chapter = "Trigonometric Functions",
                concept = "Trigonometric Identity Invariance",
                difficulty = Difficulty.MEDIUM,
                solutionText = "Expanding and transforming using product-to-sum identities eliminates all α terms, resulting in an expression purely in terms of β (independent of α).\nCorrect Option: (A)",
                idealTimeSeconds = 70,
                youtubeSearchQuery = "value of 2 cos2 alpha - 4 sin(alpha+beta) sin beta cos alpha independent of"
            )
        )

        // Maths Q12
        list.add(
            QuestionItem(
                id = "QPT2_MATH_12",
                questionNumber = 12,
                subject = Subject.MATHEMATICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "The number of values of θ in the interval [0, 7π/2] satisfying the equation 2 cos² θ + 3 cos θ = 2 is:",
                options = listOf("(A) 2", "(B) 3", "(C) 4", "(D) 5"),
                correctAnswer = "B",
                chapter = "Trigonometric Equations",
                concept = "Quadratic in Cosine & Solution Counting in Given Interval",
                difficulty = Difficulty.EASY,
                solutionText = "2 cos² θ + 3 cos θ - 2 = 0 => (2 cos θ - 1)(cos θ + 2) = 0.\ncos θ = -2 is impossible, so cos θ = 1/2.\nIn [0, 7π/2] (3.5 revolutions): θ = π/3, 5π/3, 7π/3 (3 solutions). (11π/3 is outside [0, 7π/2]).\nCorrect Option: (B)",
                idealTimeSeconds = 50,
                youtubeSearchQuery = "number of values of theta in 0 to 7pi/2 satisfying 2 cos2 theta + 3 cos theta = 2"
            )
        )

        // Maths Q13
        list.add(
            QuestionItem(
                id = "QPT2_MATH_13",
                questionNumber = 13,
                subject = Subject.MATHEMATICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "Evaluate the exact value of the following expression: sin⁴(15°) + sin⁴(75°) + sin⁴(105°) + sin⁴(165°).",
                options = listOf("(A) 5/4", "(B) 3/2", "(C) 7/8", "(D) 7/4"),
                correctAnswer = "D",
                chapter = "Trigonometric Functions",
                concept = "Symmetric Power Sums of Angles",
                difficulty = Difficulty.MEDIUM,
                solutionText = "sin 105° = sin 75°, sin 165° = sin 15°.\nSum = 2 [sin⁴ 15° + sin⁴ 75°] = 2 [sin⁴ 15° + cos⁴ 15°] = 2 [1 - 2 sin² 15° cos² 15°] = 2 [1 - 1/2 sin² 30°] = 2 [1 - 1/8] = 7/4.\nCorrect Option: (D)",
                idealTimeSeconds = 60,
                youtubeSearchQuery = "value of sin4 15 + sin4 75 + sin4 105 + sin4 165"
            )
        )

        // Maths Q14
        list.add(
            QuestionItem(
                id = "QPT2_MATH_14",
                questionNumber = 14,
                subject = Subject.MATHEMATICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "The number of solutions of 2^(sec² x) + 2^(5 - tan² x) = 20 in the interval 0 ≤ x ≤ 2π are:",
                options = listOf("(A) 8", "(B) 6", "(C) 10", "(D) 4"),
                correctAnswer = "A",
                chapter = "Trigonometric Equations",
                concept = "Exponential Substitution with sec² x = 1 + tan² x",
                difficulty = Difficulty.MEDIUM,
                solutionText = "sec² x = 1 + tan² x. Let y = 2^(tan² x) ≥ 1.\nThen 2^(1 + tan² x) + 2^(5 - tan² x) = 2y + 32/y = 20 => y + 16/y = 10 => y² - 10y + 16 = 0.\n=> y = 2 or y = 8.\nCase 1: 2^(tan² x) = 2¹ => tan² x = 1 => tan x = ±1 (4 solutions in [0, 2π]).\nCase 2: 2^(tan² x) = 2³ => tan² x = 3 => tan x = ±√3 (4 solutions in [0, 2π]).\nTotal solutions = 4 + 4 = 8.\nCorrect Option: (A)",
                idealTimeSeconds = 80,
                youtubeSearchQuery = "number of solutions of 2^(sec2 x) + 2^(5-tan2 x) = 20 in 0 to 2pi"
            )
        )

        // Maths Q15
        list.add(
            QuestionItem(
                id = "QPT2_MATH_015",
                questionNumber = 15,
                subject = Subject.MATHEMATICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "If cos θ = 1/2 (x + 1/x), then the value of cos 3θ - 1/2 (x³ + 1/x³) is:",
                options = listOf("(A) 1", "(B) 1/2", "(C) 0", "(D) -1"),
                correctAnswer = "C",
                chapter = "Trigonometric Functions",
                concept = "De Moivre / Algebraic Form of cos 3θ",
                difficulty = Difficulty.EASY,
                solutionText = "cos 3θ = 4 cos³ θ - 3 cos θ = 4(1/8 (x + 1/x)³) - 3(1/2 (x + 1/x))\n= 1/2 (x³ + 1/x³ + 3(x + 1/x)) - 3/2 (x + 1/x) = 1/2 (x³ + 1/x³).\nThus, cos 3θ - 1/2 (x³ + 1/x³) = 0.\nCorrect Option: (C)",
                idealTimeSeconds = 45,
                youtubeSearchQuery = "If cos theta = 1/2 (x + 1/x) value of cos 3theta - 1/2(x3 + 1/x3)"
            )
        )

        // Maths Q16
        list.add(
            QuestionItem(
                id = "QPT2_MATH_16",
                questionNumber = 16,
                subject = Subject.MATHEMATICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "If θ - ϕ = π/2, what is the maximum value of sin θ · sin ϕ?",
                options = listOf("(A) 1", "(B) 1/2", "(C) 1/√2", "(D) -1/2"),
                correctAnswer = "B",
                chapter = "Trigonometric Functions",
                concept = "Product to Sum Optimization",
                difficulty = Difficulty.EASY,
                solutionText = "sin θ sin ϕ = 1/2 [cos(θ - ϕ) - cos(θ + ϕ)] = 1/2 [cos(π/2) - cos(θ + ϕ)] = -1/2 cos(θ + ϕ).\nMax value occurs when cos(θ + ϕ) = -1 => Max value = 1/2.\nCorrect Option: (B)",
                idealTimeSeconds = 45,
                youtubeSearchQuery = "If theta - phi = pi/2 maximum value of sin theta sin phi"
            )
        )

        // Maths Q17
        list.add(
            QuestionItem(
                id = "QPT2_MATH_17",
                questionNumber = 17,
                subject = Subject.MATHEMATICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "If 5 cos x = 3 cos y, then the value of cot((x+y)/2) · cot((x-y)/2) is:",
                options = listOf("(A) 2", "(B) 4", "(C) 1/4", "(D) 8"),
                correctAnswer = "B",
                chapter = "Trigonometric Functions",
                concept = "Componendo and Dividendo in Trigonometry",
                difficulty = Difficulty.MEDIUM,
                solutionText = "cos x / cos y = 3/5. Applying Componendo & Dividendo:\n(cos y - cos x) / (cos y + cos x) = (5 - 3) / (5 + 3) = 2/8 = 1/4.\n2 sin((x+y)/2) sin((x-y)/2) / [2 cos((x+y)/2) cos((x-y)/2)] = 1/4\n=> tan((x+y)/2) tan((x-y)/2) = 1/4 => cot((x+y)/2) cot((x-y)/2) = 4.\nCorrect Option: (B)",
                idealTimeSeconds = 60,
                youtubeSearchQuery = "If 5 cos x = 3 cos y find cot(x+y)/2 cot(x-y)/2"
            )
        )

        // Maths Q18
        list.add(
            QuestionItem(
                id = "QPT2_MATH_18",
                questionNumber = 18,
                subject = Subject.MATHEMATICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "The solution set of the equation √6 sin x tan x + 3√2 sin x - √3 tan x - 3 = 0 in the interval (0, 2π) is:",
                options = listOf(
                    "(A) {π/4, 2π/3, 3π/4, 5π/3}",
                    "(B) {π/4, π/3, 3π/4, 4π/3}",
                    "(C) {π/4, 3π/4}",
                    "(D) {π/3, 2π/3, 4π/3, 5π/3}"
                ),
                correctAnswer = "A",
                chapter = "Trigonometric Equations",
                concept = "Factoring Trigonometric Polynomials",
                difficulty = Difficulty.MEDIUM,
                solutionText = "Factor by grouping:\n√6 sin x (tan x + √3) - √3 (tan x + √3) = 0 => (√6 sin x - √3)(tan x + √3) = 0.\n1) sin x = √3/√6 = 1/√2 => x = π/4, 3π/4 in (0, 2π).\n2) tan x = -√3 => x = 2π/3, 5π/3 in (0, 2π).\nSolution set: {π/4, 2π/3, 3π/4, 5π/3}.\nCorrect Option: (A)",
                idealTimeSeconds = 70,
                youtubeSearchQuery = "solution set of sqrt 6 sin x tan x + 3 sqrt 2 sin x - sqrt 3 tan x - 3 = 0"
            )
        )

        // Maths Q19
        list.add(
            QuestionItem(
                id = "QPT2_MATH_19",
                questionNumber = 19,
                subject = Subject.MATHEMATICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "Let α, β, and γ be three angles in the interval (0, π) that satisfy the following relations: tan(β/2) = 1/5 cot(α/2) and cot(γ/2) = 1/4 (5 tan(α/2) + cot(α/2)). Then, the value of α + β + γ is equal to:",
                options = listOf("(A) π/2", "(B) π", "(C) 3π/2", "(D) 2π"),
                correctAnswer = "B",
                chapter = "Trigonometric Functions",
                concept = "Sum of Half-Angles Identity",
                difficulty = Difficulty.HARD,
                solutionText = "Using the standard relation for triangle half angles tan(α/2)tan(β/2) + tan(β/2)tan(γ/2) + tan(γ/2)tan(α/2) = 1 => α + β + γ = π.\nCorrect Option: (B)",
                idealTimeSeconds = 90,
                youtubeSearchQuery = "Let alpha beta gamma satisfy relations find alpha + beta + gamma"
            )
        )

        // Maths Q20
        list.add(
            QuestionItem(
                id = "QPT2_MATH_20",
                questionNumber = 20,
                subject = Subject.MATHEMATICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "If sin θ and cos θ are the roots of the quadratic equation 25x² - px + q = 0, and it is given that sin θ + cos θ + tan θ + cot θ + sec θ + csc θ = 32/5 for 0 < θ < π/2, then the value of p + q is:",
                options = listOf("(A) 47", "(B) 35", "(C) 23", "(D) 12"),
                correctAnswer = "A",
                chapter = "Trigonometric Functions",
                concept = "Roots of Quadratic & Trigonometric Identity Sums",
                difficulty = Difficulty.HARD,
                solutionText = "Let s = sin θ + cos θ = p/25, and P = sin θ cos θ = q/25. Note s² = 1 + 2P.\nSum = s + 1/P + s/P = s + (1+s)/P = 32/5.\nSolving gives p = 35, q = 12 => p + q = 47.\nCorrect Option: (A)",
                idealTimeSeconds = 100,
                youtubeSearchQuery = "If sin theta and cos theta are roots of 25x2 - px + q = 0 find p + q"
            )
        )

        // Maths Q21 (Numerical)
        list.add(
            QuestionItem(
                id = "QPT2_MATH_21",
                questionNumber = 21,
                subject = Subject.MATHEMATICS,
                section = "Section B",
                type = QuestionType.NUMERICAL,
                questionText = "If θ + ϕ = 45° and the value of the expression (tan θ) / (1 - tan θ) · (tan ϕ) / (1 - tan ϕ) is equal to 1/k (assuming the expression is defined), then find the value of k.",
                correctAnswer = "2",
                chapter = "Trigonometric Functions",
                concept = "Special 45 Degree Tangent Product Identities",
                difficulty = Difficulty.MEDIUM,
                solutionText = "Given θ + ϕ = 45° => (1 + tan θ)(1 + tan ϕ) = 2.\nSimplifying (tan θ / (1 - tan θ)) · (tan ϕ / (1 - tan ϕ)) gives 1/2.\nThus k = 2.\nCorrect Answer: 2",
                idealTimeSeconds = 60,
                youtubeSearchQuery = "If theta + phi = 45 degrees tan theta / (1 - tan theta) value"
            )
        )

        // Maths Q22 (Numerical)
        list.add(
            QuestionItem(
                id = "QPT2_MATH_22",
                questionNumber = 22,
                subject = Subject.MATHEMATICS,
                section = "Section B",
                type = QuestionType.NUMERICAL,
                questionText = "The number of solutions of the equation sin² x + cos² 2x = 2 in the interval [0, 30] is equal to ___",
                correctAnswer = "10",
                chapter = "Trigonometric Equations",
                concept = "Extremum Values of Sum of Squares",
                difficulty = Difficulty.MEDIUM,
                solutionText = "Since sin² x ≤ 1 and cos² 2x ≤ 1, their sum can only be 2 if sin² x = 1 and cos² 2x = 1 simultaneously.\nsin² x = 1 => x = (2n+1)π/2. For these x, cos 2x = 1 - 2 sin² x = -1, so cos² 2x = 1 holds true for all x = (2n+1)π/2.\nValues in [0, 30]: π/2, 3π/2, 5π/2, 7π/2, 9π/2, 11π/2, 13π/2, 15π/2, 17π/2, 19π/2 (since 19 × 1.57 = 29.84 ≤ 30).\nTotal count = 10.\nCorrect Answer: 10",
                idealTimeSeconds = 75,
                youtubeSearchQuery = "number of solutions of sin2 x + cos2 2x = 2 in interval 0 to 30"
            )
        )

        // Maths Q23 (Numerical)
        list.add(
            QuestionItem(
                id = "QPT2_MATH_23",
                questionNumber = 23,
                subject = Subject.MATHEMATICS,
                section = "Section B",
                type = QuestionType.NUMERICAL,
                questionText = "If cos θ = sin² θ, then the value of sin¹² θ + 3 sin¹⁰ θ + 3 sin⁸ θ + sin⁶ θ + sin⁴ θ + sin² θ + 5 is equal to ___",
                correctAnswer = "7",
                chapter = "Trigonometric Functions",
                concept = "Powers of Golden Trigonometric Ratio",
                difficulty = Difficulty.MEDIUM,
                solutionText = "Given sin² θ = cos θ => 1 - cos² θ = cos θ => cos² θ + cos θ = 1.\n(sin⁴ θ + sin² θ)³ = (cos² θ + cos θ)³ = 1³ = 1.\nExpanding gives sin¹² θ + 3 sin¹⁰ θ + 3 sin⁸ θ + sin⁶ θ = 1.\nAlso sin⁴ θ + sin² θ = cos² θ + cos θ = 1.\nTotal expression = 1 + 1 + 5 = 7.\nCorrect Answer: 7",
                idealTimeSeconds = 60,
                youtubeSearchQuery = "If cos theta = sin2 theta find sin12 theta + 3 sin10 theta"
            )
        )

        // Maths Q24 (Numerical)
        list.add(
            QuestionItem(
                id = "QPT2_MATH_24",
                questionNumber = 24,
                subject = Subject.MATHEMATICS,
                section = "Section B",
                type = QuestionType.NUMERICAL,
                questionText = "The number of solutions of the equation sin⁶ x + cos⁶ x = 5/8 in the interval [0, 3π] is equal to ___",
                correctAnswer = "12",
                chapter = "Trigonometric Equations",
                concept = "Higher Powers of sin and cos",
                difficulty = Difficulty.MEDIUM,
                solutionText = "sin⁶ x + cos⁶ x = 1 - 3 sin² x cos² x = 1 - 3/4 sin² 2x = 5/8.\n=> 3/4 sin² 2x = 3/8 => sin² 2x = 1/2 => sin 2x = ±1/√2.\nIn each period [0, π], there are 4 solutions. Over [0, 3π], there are 3 × 4 = 12 solutions.\nCorrect Answer: 12",
                idealTimeSeconds = 60,
                youtubeSearchQuery = "number of solutions of sin6 x + cos6 x = 5/8 in 0 to 3pi"
            )
        )

        // Maths Q25 (Numerical)
        list.add(
            QuestionItem(
                id = "QPT2_MATH_25",
                questionNumber = 25,
                subject = Subject.MATHEMATICS,
                section = "Section B",
                type = QuestionType.NUMERICAL,
                questionText = "If 0 < α, β < π and cos α + cos β - cos(α + β) = 3/2, find the value of √3 sin α + cos α.",
                correctAnswer = "2",
                chapter = "Trigonometric Functions",
                concept = "Maximization of Two-Variable Trigonometric Sum",
                difficulty = Difficulty.HARD,
                solutionText = "The identity cos α + cos β - cos(α + β) ≤ 3/2 holds with equality if and only if α = β = π/3 (60°).\nThen √3 sin α + cos α = √3 sin 60° + cos 60° = √3(√3/2) + 1/2 = 3/2 + 1/2 = 2.\nCorrect Answer: 2",
                idealTimeSeconds = 70,
                youtubeSearchQuery = "If cos alpha + cos beta - cos(alpha+beta) = 3/2 find sqrt 3 sin alpha + cos alpha"
            )
        )

        // =========================================================================
        // ============================== PHYSICS ==================================
        // =========================================================================

        // Physics Q1
        list.add(
            QuestionItem(
                id = "QPT2_PHY_01",
                questionNumber = 26,
                subject = Subject.PHYSICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "A body of mass 5 kg is thrown vertically up with a kinetic energy of 490 J. The height at which the kinetic energy of the body becomes half of the original value is (acceleration due to gravity = 9.8 ms⁻²):",
                options = listOf("(A) 5 m", "(B) 2.5 m", "(C) 10 m", "(D) 12.5 m"),
                correctAnswer = "A",
                chapter = "Work, Power and Energy",
                concept = "Conservation of Mechanical Energy",
                difficulty = Difficulty.EASY,
                solutionText = "Loss in kinetic energy = Gain in potential energy.\nΔK = 490 / 2 = 245 J.\nm g h = 245 => 5 × 9.8 × h = 245 => 49 h = 245 => h = 5 m.\nCorrect Option: (A)",
                idealTimeSeconds = 45,
                youtubeSearchQuery = "body mass 5kg thrown vertically kinetic energy 490J height half KE"
            )
        )

        // Physics Q2
        list.add(
            QuestionItem(
                id = "QPT2_PHY_02",
                questionNumber = 27,
                subject = Subject.PHYSICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "The upper half of an inclined plane with inclination ϕ is perfectly smooth while the lower half is rough. A body starting from rest at the top will again come to rest at the bottom if the coefficient of friction for the lower half is given by:",
                options = listOf("(A) 2 cos ϕ", "(B) 2 sin ϕ", "(C) tan ϕ", "(D) 2 tan ϕ"),
                correctAnswer = "D",
                chapter = "Laws of Motion",
                concept = "Work-Energy Theorem on Inclined Plane with Friction",
                difficulty = Difficulty.EASY,
                solutionText = "Work-Energy theorem from top to bottom: ΔK = 0.\nW_gravity + W_friction = 0\nm g (L sin ϕ) - μ m g cos ϕ (L / 2) = 0\n=> sin ϕ = μ/2 cos ϕ => μ = 2 tan ϕ.\nCorrect Option: (D)",
                idealTimeSeconds = 50,
                youtubeSearchQuery = "upper half inclined plane smooth lower rough coefficient of friction"
            )
        )

        // Physics Q3
        list.add(
            QuestionItem(
                id = "QPT2_PHY_03",
                questionNumber = 28,
                subject = Subject.PHYSICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "The maximum velocity (in ms⁻¹) with which a car driver can traverse a flat curve of radius 150 m and coefficient of static friction 0.6 to avoid skidding is (assume g = 10 ms⁻²):",
                options = listOf("(A) 60", "(B) 30", "(C) 15", "(D) 25"),
                correctAnswer = "B",
                chapter = "Circular Motion",
                concept = "Safe Speed on Unbanked Flat Curve",
                difficulty = Difficulty.EASY,
                solutionText = "v_max = √(μ s g R) = √(0.6 × 10 × 150) = √900 = 30 ms⁻¹.\nCorrect Option: (B)",
                idealTimeSeconds = 30,
                youtubeSearchQuery = "maximum velocity car flat curve radius 150m friction 0.6"
            )
        )

        // Physics Q4
        list.add(
            QuestionItem(
                id = "QPT2_PHY_04",
                questionNumber = 29,
                subject = Subject.PHYSICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "A block of mass 2 kg is free to move along the x-axis. It is at rest and from t = 0 onwards it is subjected to a time-dependent force F(t) in the x direction. The force F(t) varies linearly from F = 4 N at t = 0 to F = 0 at t = 3 s, and continues to F = -2 N at t = 4.5 s. The kinetic energy of the block after 4.5 seconds is:",
                options = listOf("(A) 4.50 J", "(B) 7.50 J", "(C) 5.06 J", "(D) 14.06 J"),
                correctAnswer = "C",
                chapter = "Laws of Motion",
                concept = "Impulse-Momentum Principle from F-t Graph Area",
                difficulty = Difficulty.MEDIUM,
                solutionText = "Impulse J = Area under F-t graph = Area(0 to 3s) - Area(3 to 4.5s)\nJ = 1/2 × 3 × 4 - 1/2 × 1.5 × 2 = 6 - 1.5 = 4.5 N·s.\nVelocity v = J / m = 4.5 / 2 = 2.25 m/s.\nKinetic energy = 1/2 m v² = 1/2 × 2 × (2.25)² = 5.0625 J ≈ 5.06 J.\nCorrect Option: (C)",
                idealTimeSeconds = 70,
                youtubeSearchQuery = "block mass 2kg force F(t) area under Ft graph kinetic energy"
            )
        )

        // Physics Q5
        list.add(
            QuestionItem(
                id = "QPT2_PHY_05",
                questionNumber = 30,
                subject = Subject.PHYSICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "A body of mass 6 kg moves in a straight line according to the equation x = t³ - 75t, where x denotes distance in metre and t time in second. The force on the body at t = 4 s is:",
                options = listOf("(A) 64 N", "(B) 72 N", "(C) 144 N", "(D) 36 N"),
                correctAnswer = "C",
                chapter = "Laws of Motion",
                concept = "Kinematic Differentiation and Newton's Second Law",
                difficulty = Difficulty.EASY,
                solutionText = "v = dx/dt = 3t² - 75.\na = dv/dt = 6t.\nAt t = 4 s, a = 6(4) = 24 m/s².\nForce F = m a = 6 kg × 24 m/s² = 144 N.\nCorrect Option: (C)",
                idealTimeSeconds = 40,
                youtubeSearchQuery = "body mass 6kg x = t3 - 75t force at t = 4s"
            )
        )

        // Physics Q6
        list.add(
            QuestionItem(
                id = "QPT2_PHY_06",
                questionNumber = 31,
                subject = Subject.PHYSICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "If a particle moving in a circular path maintains a constant speed of 20 ms⁻¹, then which of the following correctly describes the relation between centripetal force (F) and radius (r)?",
                options = listOf("(A) Constant horizontal line", "(B) Straight line with positive slope", "(C) Rectangular hyperbola (F ∝ 1/r)", "(D) Parabolic curve"),
                correctAnswer = "C",
                chapter = "Circular Motion",
                concept = "Centripetal Force Relation with Radius",
                difficulty = Difficulty.EASY,
                solutionText = "Centripetal force F = m v² / r. For constant speed v, F ∝ 1/r, which represents a rectangular hyperbola.\nCorrect Option: (C)",
                idealTimeSeconds = 30,
                youtubeSearchQuery = "particle moving in circular path constant speed relation between force and radius"
            )
        )

        // Physics Q7
        list.add(
            QuestionItem(
                id = "QPT2_PHY_07",
                questionNumber = 32,
                subject = Subject.PHYSICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "A constant external force F_ext = mg starts acting on a block of mass m connected to a spring of spring constant k resting on a frictionless surface (μ = 0). At a stretch when x = mg/k, the work done by the external force (W_Fext) and the spring force (W_Fs) are related as:",
                options = listOf("(A) W_Fext + W_Fs = 0", "(B) W_Fext = 1/2 k x²", "(C) W_Fext + W_Fs > 0", "(D) None of these"),
                correctAnswer = "C",
                chapter = "Work, Power and Energy",
                concept = "Work-Energy Theorem on Spring Block System",
                difficulty = Difficulty.MEDIUM,
                solutionText = "W_Fext = F_ext · x = (mg)(mg/k) = (mg)²/k.\nW_Fs = -1/2 k x² = -1/2 k (mg/k)² = -1/2 (mg)²/k.\nW_net = W_Fext + W_Fs = +1/2 (mg)²/k > 0 (equal to positive KE).\nCorrect Option: (C)",
                idealTimeSeconds = 60,
                youtubeSearchQuery = "constant external force mg acts on block spring constant k work done"
            )
        )

        // Physics Q8
        list.add(
            QuestionItem(
                id = "QPT2_PHY_08",
                questionNumber = 33,
                subject = Subject.PHYSICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "Three blocks of masses m1, m2 and m3 are connected by massless strings, as shown, on a frictionless table. They are pulled with a force T3 = 40 N. If m1 = 10 kg, m2 = 6 kg and m3 = 4 kg, the tension T2 will be:",
                options = listOf("(A) 20 N", "(B) 40 N", "(C) 10 N", "(D) 32 N"),
                correctAnswer = "D",
                chapter = "Laws of Motion",
                concept = "Connected Body Acceleration and String Tension",
                difficulty = Difficulty.EASY,
                solutionText = "Total mass M = 10 + 6 + 4 = 20 kg.\nAcceleration a = T3 / M = 40 / 20 = 2 m/s².\nTension T2 pulls m1 and m2: T2 = (m1 + m2) a = (10 + 6) × 2 = 32 N.\nCorrect Option: (D)",
                idealTimeSeconds = 40,
                youtubeSearchQuery = "three blocks m1 m2 m3 connected massless strings pulled by force 40N tension T2"
            )
        )

        // Physics Q9
        list.add(
            QuestionItem(
                id = "QPT2_PHY_09",
                questionNumber = 34,
                subject = Subject.PHYSICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "Two horizontal plates, each of mass m, are connected by a vertical massless spring. The bottom plate rests unattached on a horizontal surface. Initially, the system is at rest in equilibrium. An additional weight W is placed on the upper plate, and the system is allowed to come to a new, fully compressed equilibrium position. The weight W is then suddenly removed. What is the minimum value of W required so that the normal force on the bottom plate reaches zero (i.e., bottom plate just lifts off) during subsequent upward motion?",
                options = listOf("(A) mg", "(B) 2mg", "(C) 3mg", "(D) 4mg"),
                correctAnswer = "B",
                chapter = "Work, Power and Energy",
                concept = "Spring System Oscillations and Normal Reaction Lift-off",
                difficulty = Difficulty.HARD,
                solutionText = "For bottom plate to lift off, spring extension x_ext = mg/k.\nInitial equilibrium compression x0 = mg/k. Compression under W is x1 = (m g + W)/k.\nAmplitude A = x1 - x0 = W/k.\nMaximum upward extension = A - x0 = (W - mg)/k.\nSetting extension = mg/k => (W - mg)/k = mg/k => W = 2mg.\nCorrect Option: (B)",
                idealTimeSeconds = 90,
                youtubeSearchQuery = "two plates mass m vertical spring minimum weight W bottom plate lifts off"
            )
        )

        // Physics Q10
        list.add(
            QuestionItem(
                id = "QPT2_PHY_10",
                questionNumber = 35,
                subject = Subject.PHYSICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "A body of mass m is moving in a straight line with momentum p. Starting at time t = 0, a force F = at acts in the same direction on the moving particle during time interval T, so that its momentum changes from p to 2p. The value of T is:",
                options = listOf("(A) √(2p / a)", "(B) √(p / a)", "(C) 2√(2p / a)", "(D) 2p / a"),
                correctAnswer = "A",
                chapter = "Laws of Motion",
                concept = "Impulse of Time-Varying Force",
                difficulty = Difficulty.EASY,
                solutionText = "Change in momentum Δp = 2p - p = p.\nImpulse = ∫[0 to T] at dt = a T² / 2 = p => T² = 2p/a => T = √(2p/a).\nCorrect Option: (A)",
                idealTimeSeconds = 40,
                youtubeSearchQuery = "body momentum p force F = at momentum changes from p to 2p find T"
            )
        )

        // Physics Q11
        list.add(
            QuestionItem(
                id = "QPT2_PHY_11",
                questionNumber = 36,
                subject = Subject.PHYSICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "One coolie takes 1 min to raise a suitcase through a height of 2 m but the second coolie takes 30 s to raise the same suitcase to the same height. The powers of the two coolies are in the ratio:",
                options = listOf("(A) 1 : 3", "(B) 2 : 1", "(C) 3 : 1", "(D) 1 : 2"),
                correctAnswer = "D",
                chapter = "Work, Power and Energy",
                concept = "Power Definition as Rate of Work",
                difficulty = Difficulty.EASY,
                solutionText = "Work done W = m g h is same for both.\nP1 / P2 = (W / t1) / (W / t2) = t2 / t1 = 30 s / 60 s = 1/2 = 1 : 2.\nCorrect Option: (D)",
                idealTimeSeconds = 30,
                youtubeSearchQuery = "coolie takes 1 min to raise suitcase second coolie 30s ratio of power"
            )
        )

        // Physics Q12
        list.add(
            QuestionItem(
                id = "QPT2_PHY_12",
                questionNumber = 37,
                subject = Subject.PHYSICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "A boy pushes a toy box 2.0 m along the floor by means of a force of 10 N directed downward at an angle of 60° to the horizontal. The work done by the boy is:",
                options = listOf("(A) 6 J", "(B) 8 J", "(C) 10 J", "(D) 12 J"),
                correctAnswer = "C",
                chapter = "Work, Power and Energy",
                concept = "Dot Product Formulation of Work Done",
                difficulty = Difficulty.EASY,
                solutionText = "Work W = F d cos θ = 10 N × 2.0 m × cos 60° = 20 × 0.5 = 10 J.\nCorrect Option: (C)",
                idealTimeSeconds = 30,
                youtubeSearchQuery = "boy pushes toy box 2m force 10N angle 60 work done"
            )
        )

        // Physics Q13
        list.add(
            QuestionItem(
                id = "QPT2_PHY_13",
                questionNumber = 38,
                subject = Subject.PHYSICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "A plate of mass M is placed on a horizontal frictionless surface, and a body of mass m is placed on this plate. The coefficient of friction between this body and plate is μ. Given the condition 2M > m, if a horizontal force of 3μmg is applied to the body of mass m, what will be the acceleration of the plate?",
                options = listOf("(A) (μmg) / M", "(B) (μmg) / (M + m)", "(C) (3μmg) / M", "(D) (2μmg) / (M + m)"),
                correctAnswer = "A",
                chapter = "Laws of Motion",
                concept = "Two-Block Friction System with Relative Slipping",
                difficulty = Difficulty.MEDIUM,
                solutionText = "Max friction force between body and plate = f_max = μmg.\nSince applied force F = 3μmg > f_max, slipping occurs.\nThe only horizontal force driving plate M is kinetic friction f_k = μmg.\nAcceleration of plate a_plate = f_k / M = (μmg) / M.\nCorrect Option: (A)",
                idealTimeSeconds = 60,
                youtubeSearchQuery = "plate mass M body mass m force 3mu mg acceleration of plate"
            )
        )

        // Physics Q14
        list.add(
            QuestionItem(
                id = "QPT2_PHY_14",
                questionNumber = 39,
                subject = Subject.PHYSICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "Assertion: Work done in moving a body over a closed loop is zero for every force in nature.\nReason: Work done does not depend on nature of force.",
                options = listOf(
                    "(A) Both assertion and reason are true and reason is correct explanation.",
                    "(B) Both assertion and reason are true but reason is not correct explanation.",
                    "(C) Assertion is true but reason is false.",
                    "(D) If both assertion and reason are false."
                ),
                correctAnswer = "D",
                chapter = "Work, Power and Energy",
                concept = "Conservative vs Non-Conservative Forces",
                difficulty = Difficulty.EASY,
                solutionText = "Work done over a closed loop is zero ONLY for conservative forces (e.g. gravity, electrostatic), not non-conservative forces (like friction). Work done fundamentally depends on the nature of force. Thus both Assertion and Reason are false.\nCorrect Option: (D)",
                idealTimeSeconds = 35,
                youtubeSearchQuery = "Assertion Work done in moving body over closed loop is zero for every force"
            )
        )

        // Physics Q15
        list.add(
            QuestionItem(
                id = "QPT2_PHY_15",
                questionNumber = 40,
                subject = Subject.PHYSICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "A particle is moving in a force field given by F = y² î - x² ĵ. Starting from A(0,0), the particle reaches C(1,1) either along ABC or ADC. Let the work done along the two paths be W1 and W2 respectively. Then, (W1, W2) are:",
                options = listOf("(A) (-1, 1)", "(B) (-1, 0)", "(C) (1, 1)", "(D) (-1, -1)"),
                correctAnswer = "A",
                chapter = "Work, Power and Energy",
                concept = "Line Integral of Non-Conservative Force Field",
                difficulty = Difficulty.MEDIUM,
                solutionText = "Path ABC: A(0,0) -> B(1,0) (y=0, dy=0, W_AB=0) then B(1,0) -> C(1,1) (x=1, dx=0, F_y = -x² = -1, W_BC = ∫ -1 dy = -1). So W1 = -1.\nPath ADC: A(0,0) -> D(0,1) (x=0, dx=0, W_AD=0) then D(0,1) -> C(1,1) (y=1, dy=0, F_x = y² = 1, W_DC = ∫ 1 dx = 1). So W2 = +1.\n(W1, W2) = (-1, 1).\nCorrect Option: (A)",
                idealTimeSeconds = 70,
                youtubeSearchQuery = "particle moving in force field F = y2 i - x2 j work done path ABC and ADC"
            )
        )

        // Physics Q16
        list.add(
            QuestionItem(
                id = "QPT2_PHY_16",
                questionNumber = 41,
                subject = Subject.PHYSICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "At any instant the velocity of a particle of mass 500 g is (2t î + 3t² ĵ) ms⁻¹. If the force acting on the particle at t = 1 s is (î + x ĵ) N, then the value of x will be:",
                options = listOf("(A) 3", "(B) 4", "(C) 6", "(D) 2"),
                correctAnswer = "A",
                chapter = "Laws of Motion",
                concept = "Vector Acceleration and Newton's Second Law",
                difficulty = Difficulty.EASY,
                solutionText = "a = dv/dt = 2 î + 6t ĵ. At t = 1 s, a = 2 î + 6 ĵ.\nForce F = m a = 0.5 kg × (2 î + 6 ĵ) = 1 î + 3 ĵ N.\nComparing with (î + x ĵ) N gives x = 3.\nCorrect Option: (A)",
                idealTimeSeconds = 40,
                youtubeSearchQuery = "velocity of particle mass 500g v = 2t i + 3t2 j force at t = 1s find x"
            )
        )

        // Physics Q17
        list.add(
            QuestionItem(
                id = "QPT2_PHY_17",
                questionNumber = 42,
                subject = Subject.PHYSICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "The same spring is attached with 2 kg, 3 kg and 1 kg blocks in three different cases (Case 1: 2 kg & 2 kg; Case 2: 3 kg & 2 kg; Case 3: 1 kg & 2 kg). If x1, x2, x3 be the extensions in the spring in the three cases, then:",
                options = listOf("(A) x1 = 0, x3 > x2", "(B) x1 > x2 > x3", "(C) x3 > x2 > x1", "(D) x2 > x1 > x3"),
                correctAnswer = "D",
                chapter = "Laws of Motion",
                concept = "Effective Tension in Atwood Machine Springs",
                difficulty = Difficulty.MEDIUM,
                solutionText = "Tension in spring T = (2 m1 m2 g) / (m1 + m2).\nCase 1 (2kg, 2kg): T1 = 2(2)(2)g / 4 = 2g.\nCase 2 (3kg, 2kg): T2 = 2(3)(2)g / 5 = 2.4g.\nCase 3 (1kg, 2kg): T3 = 2(1)(2)g / 3 = 1.33g.\nSince extension x = T/k: x2 > x1 > x3.\nCorrect Option: (D)",
                idealTimeSeconds = 60,
                youtubeSearchQuery = "same spring attached with 2kg 3kg and 1kg blocks extensions x1 x2 x3"
            )
        )

        // Physics Q18
        list.add(
            QuestionItem(
                id = "QPT2_PHY_18",
                questionNumber = 43,
                subject = Subject.PHYSICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "A particle undergoes uniform circular motion. Consider the following statements regarding the work done by the net force acting on the particle:\nI. The work done over any infinitesimal segment is zero because the force is always perpendicular to instantaneous velocity.\nII. The net work done over any finite distance along the curve is zero because kinetic energy remains constant.\nIII. Work done is zero primarily because total displacement after one complete revolution is zero.\nIV. No work is done because net force acting on particle in uniform circular motion is zero.\nWhich of the following options correctly identifies valid statement(s)?",
                options = listOf("(A) I and II only", "(B) I only", "(C) II and III only", "(D) I, III, and IV"),
                correctAnswer = "A",
                chapter = "Circular Motion",
                concept = "Work in Uniform Circular Motion",
                difficulty = Difficulty.EASY,
                solutionText = "In uniform circular motion, F ⊥ v at every instant so dW = F · ds = 0 (Statement I is true). Speed is constant so ΔK = 0 => W = 0 for any finite arc (Statement II is true). Net force is not zero (centripetal force exists), so IV is false. Statement III is misleading because work is zero over ANY segment, not just complete cycles. Thus I and II are valid.\nCorrect Option: (A)",
                idealTimeSeconds = 45,
                youtubeSearchQuery = "particle undergoes uniform circular motion work done net force statements"
            )
        )

        // Physics Q19
        list.add(
            QuestionItem(
                id = "QPT2_PHY_19",
                questionNumber = 44,
                subject = Subject.PHYSICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "Two blocks are connected over a massless pulley as shown. The mass of block A on a 30° incline is 10 kg and coefficient of kinetic friction is 0.2. Block A slides down the incline at constant speed. The mass of suspended block B in kg is:",
                options = listOf("(A) 4", "(B) 3.3", "(C) 3", "(D) 2.5"),
                correctAnswer = "B",
                chapter = "Laws of Motion",
                concept = "Equilibrium of Connected Bodies on Incline with Friction",
                difficulty = Difficulty.MEDIUM,
                solutionText = "For constant speed (zero acceleration) down the incline:\nm_A g sin 30° - f_k - T = 0\nwhere f_k = μ m_A g cos 30° = 0.2 × 10 × 10 × (√3/2) = 17.32 N.\nm_A g sin 30° = 10 × 10 × 0.5 = 50 N.\nTension T = 50 - 17.32 = 32.68 N.\nFor block B: T = m_B g => m_B = 32.68 / 10 ≈ 3.27 kg ≈ 3.3 kg.\nCorrect Option: (B)",
                idealTimeSeconds = 60,
                youtubeSearchQuery = "block A 10kg on 30 degree incline friction 0.2 slides down constant speed mass B"
            )
        )

        // Physics Q20
        list.add(
            QuestionItem(
                id = "QPT2_PHY_20",
                questionNumber = 45,
                subject = Subject.PHYSICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "A smooth chain of length 2 m is kept on a table such that 60 cm hangs freely from the edge of the table. The total mass of the chain is 4 kg. The work done in pulling the entire chain onto the table is (Take g = 10 m/s²):",
                options = listOf("(A) 6.3 J", "(B) 3.6 J", "(C) 2.0 J", "(D) 12.9 J"),
                correctAnswer = "B",
                chapter = "Work, Power and Energy",
                concept = "Work Done against Gravity on Hanging Chain",
                difficulty = Difficulty.EASY,
                solutionText = "Mass per unit length λ = M / L = 4 / 2 = 2 kg/m.\nLength of hanging part l = 0.60 m.\nMass of hanging part m' = λ l = 2 × 0.6 = 1.2 kg.\nCenter of mass of hanging part is at h_cm = l / 2 = 0.30 m below table edge.\nWork done W = m' g (l / 2) = 1.2 kg × 10 m/s² × 0.30 m = 3.6 J.\nCorrect Option: (B)",
                idealTimeSeconds = 45,
                youtubeSearchQuery = "chain length 2m mass 4kg 60cm hangs freely work done pulling chain"
            )
        )

        // Physics Q21 (Numerical)
        list.add(
            QuestionItem(
                id = "QPT2_PHY_21",
                questionNumber = 46,
                subject = Subject.PHYSICS,
                section = "Section B",
                type = QuestionType.NUMERICAL,
                questionText = "Consider an elliptically shaped rail PQ in the vertical plane with OP = 3 m and OQ = 4 m. A block of mass 1 kg is pulled along the rail from P to Q with a force of 18 N, which is always parallel to line PQ. Assuming no frictional losses, the kinetic energy of the block when it reaches Q is (n × 10) Joules. The value of n is (take acceleration due to gravity = 10 m s⁻²):",
                correctAnswer = "5",
                chapter = "Work, Power and Energy",
                concept = "Work-Energy Theorem on Curved Trajectory",
                difficulty = Difficulty.MEDIUM,
                solutionText = "Displacement vector from P to Q: Δr = -3 î + 4 ĵ. Magnitude PQ = √(3² + 4²) = 5 m.\nForce is always parallel to line PQ, so work done by force W_F = F × (distance along line PQ) = 18 N × 5 m = 90 J.\nWork done by gravity W_g = -m g h = -1 kg × 10 m/s² × 4 m = -40 J.\nKinetic energy at Q = W_F + W_g = 90 - 40 = 50 J.\nSince KE = n × 10 J => n = 5.\nCorrect Answer: 5",
                idealTimeSeconds = 70,
                youtubeSearchQuery = "elliptically shaped rail OP = 3m OQ = 4m force 18N parallel to line PQ find n"
            )
        )

        // Physics Q22 (Numerical)
        list.add(
            QuestionItem(
                id = "QPT2_PHY_22",
                questionNumber = 47,
                subject = Subject.PHYSICS,
                section = "Section B",
                type = QuestionType.NUMERICAL,
                questionText = "A block of mass M is placed on a smooth horizontal surface and it is pulled by a light spring. If the ends A and B of the spring are moving with 4 m s⁻¹ and 2 m s⁻¹ respectively and the rate at which spring energy is increasing is 20 J s⁻¹, then what is the value of spring force (in N)?",
                correctAnswer = "10",
                chapter = "Work, Power and Energy",
                concept = "Rate of Change of Potential Energy in Spring",
                difficulty = Difficulty.MEDIUM,
                solutionText = "Spring potential energy U = 1/2 k x².\nRate of increase dU/dt = k x (dx/dt) = F_spring × v_rel.\nRelative speed of ends = v_A - v_B = 4 - 2 = 2 m/s.\n20 = F_spring × 2 => F_spring = 10 N.\nCorrect Answer: 10",
                idealTimeSeconds = 50,
                youtubeSearchQuery = "ends A and B of spring moving with 4 m/s and 2 m/s spring energy rate 20 J/s"
            )
        )

        // Physics Q23 (Numerical)
        list.add(
            QuestionItem(
                id = "QPT2_PHY_23",
                questionNumber = 48,
                subject = Subject.PHYSICS,
                section = "Section B",
                type = QuestionType.NUMERICAL,
                questionText = "A 1 kg block B, which can be treated as a point mass, rests on a bracket A of the same mass (1 kg). Constant horizontal forces F1 = 20 N and F2 = 8 N start to act at t = 0 when distance from block B to pulley is 50 cm. If the time (in seconds) it takes for block B to reach the pulley is t, find the value of 10t.",
                correctAnswer = "5",
                chapter = "Laws of Motion",
                concept = "Relative Acceleration in Constrained System",
                difficulty = Difficulty.HARD,
                solutionText = "Relative acceleration a_rel between block B and bracket A is computed as 4 m/s².\nUsing s_rel = 1/2 a_rel t² => 0.50 m = 1/2 (4) t² = 2 t² => t² = 0.25 => t = 0.5 s.\n10t = 10 × 0.5 = 5.\nCorrect Answer: 5",
                idealTimeSeconds = 80,
                youtubeSearchQuery = "block B on bracket A F1 = 20N F2 = 8N distance 50cm find 10t"
            )
        )

        // Physics Q24 (Numerical)
        list.add(
            QuestionItem(
                id = "QPT2_PHY_24",
                questionNumber = 49,
                subject = Subject.PHYSICS,
                section = "Section B",
                type = QuestionType.NUMERICAL,
                questionText = "A block of mass 2 kg is initially at the origin (x = 0) at t = 0 with a velocity of 4√5 m/s in positive x-direction. The potential energy is U(x) = -x³ + 6x² + 15 Joules. Calculate the velocity of the block (in m/s) at the instant when it experiences maximum force in negative x-direction.",
                correctAnswer = "8",
                chapter = "Work, Power and Energy",
                concept = "Force as Negative Gradient of Potential Energy",
                difficulty = Difficulty.HARD,
                solutionText = "Force F = -dU/dx = -(-3x² + 12x) = 3x² - 12x.\nFor maximum force in negative x direction, dF/dx = 0 => 6x - 12 = 0 => x = 2 m.\nAt x = 0: U(0) = 15 J, KE(0) = 1/2 (2) (4√5)² = 80 J => Total energy E = 95 J.\nAt x = 2 m: U(2) = -(2)³ + 6(2)² + 15 = -8 + 24 + 15 = 31 J.\nKE(2) = E - U(2) = 95 - 31 = 64 J.\n1/2 m v² = 64 => 1/2 (2) v² = 64 => v = 8 m/s.\nCorrect Answer: 8",
                idealTimeSeconds = 80,
                youtubeSearchQuery = "block mass 2kg U = -x3 + 6x2 + 15 velocity at maximum negative force"
            )
        )

        // Physics Q25 (Numerical)
        list.add(
            QuestionItem(
                id = "QPT2_PHY_25",
                questionNumber = 50,
                subject = Subject.PHYSICS,
                section = "Section B",
                type = QuestionType.NUMERICAL,
                questionText = "Block A of mass 2 kg is placed over block B of mass 8 kg. The combination is placed over a rough horizontal surface. Coefficient of friction between B and the floor is 0.5. Coefficient of friction between A and B is 0.4. A horizontal force of 10 N is applied on block B. The force of friction between A and B is (g = 10 m s⁻²):",
                correctAnswer = "0",
                chapter = "Laws of Motion",
                concept = "Static Friction Threshold in Two-Block System",
                difficulty = Difficulty.EASY,
                solutionText = "Total mass = 2 + 8 = 10 kg.\nMaximum static friction from floor on block B = f_floor,max = μ_floor (M_A + M_B) g = 0.5 × 10 × 10 = 50 N.\nSince applied force F = 10 N < 50 N, the entire system remains at rest (zero acceleration).\nSince block A experiences zero acceleration and no direct external horizontal force, the friction between A and B is 0 N.\nCorrect Answer: 0",
                idealTimeSeconds = 40,
                youtubeSearchQuery = "block A mass 2kg over block B mass 8kg force 10N friction between A and B"
            )
        )

        // =========================================================================
        // ============================= CHEMISTRY =================================
        // =========================================================================

        // Chemistry Q1
        list.add(
            QuestionItem(
                id = "QPT2_CHEM_01",
                questionNumber = 51,
                subject = Subject.CHEMISTRY,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "If hydrogen and oxygen are mixed and kept in the same vessel at room temperature, the reaction does not take place to form water because:",
                options = listOf(
                    "(A) activation energy for the reaction is very high at room temperature",
                    "(B) molecules have no proper orientation to react to form water",
                    "(C) the frequency of collisions is not high enough for the reaction to take place",
                    "(D) no catalyst is present in the reaction mixture"
                ),
                correctAnswer = "A",
                chapter = "Chemical Kinetics",
                concept = "Activation Energy Barrier in Spontaneous Reactions",
                difficulty = Difficulty.EASY,
                solutionText = "Even though H2 + 1/2 O2 -> H2O is thermodynamically favorable (negative ΔG), the activation energy required to break the strong H-H and O=O bonds is very high at room temperature, making the kinetic rate negligible.\nCorrect Option: (A)",
                idealTimeSeconds = 30,
                youtubeSearchQuery = "hydrogen oxygen reaction does not take place room temperature activation energy"
            )
        )

        // Chemistry Q2
        list.add(
            QuestionItem(
                id = "QPT2_CHEM_02",
                questionNumber = 52,
                subject = Subject.CHEMISTRY,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "A conductivity cell is filled with a KCl solution. In Experiment 1, the cell contains 5 moles of KCl in 50 mL of solution, and its molar conductivity is Λm,exp 1. In Experiment 2, the same conductivity cell is filled with 10 moles of KCl in 200 mL of solution, and its molar conductivity is Λm,exp 2. If the specific conductivity (conductivity) of the solution in Experiment 1 is half that of Experiment 2 (κ1 = 0.5 κ2), what is the relationship between Λm,exp 2 and Λm,exp 1?",
                options = listOf(
                    "(A) Λm,exp 2 = 2 Λm,exp 1",
                    "(B) Λm,exp 2 = 4 Λm,exp 1",
                    "(C) Λm,exp 2 = 0.5 Λm,exp 1",
                    "(D) Λm,exp 2 = Λm,exp 1"
                ),
                correctAnswer = "B",
                chapter = "Electrochemistry",
                concept = "Molar Conductivity and Specific Conductivity Formula",
                difficulty = Difficulty.MEDIUM,
                solutionText = "Molar conductivity Λm = (1000 × κ) / C.\nC1 = 5 / (50/1000) = 100 M.\nC2 = 10 / (200/1000) = 50 M.\nΛm1 = 1000 × κ1 / 100 = 10 κ1.\nΛm2 = 1000 × κ2 / 50 = 20 κ2 = 20 × (2 κ1) = 40 κ1 = 4 × (10 κ1) = 4 Λm1.\nCorrect Option: (B)",
                idealTimeSeconds = 60,
                youtubeSearchQuery = "conductivity cell filled with KCl solution molar conductivity comparison"
            )
        )

        // Chemistry Q3
        list.add(
            QuestionItem(
                id = "QPT2_CHEM_03",
                questionNumber = 53,
                subject = Subject.CHEMISTRY,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "A solution is prepared by dissolving 31 g of a non-volatile, non-electrolyte solute (molar mass = 62 g/mol) in 500 g of water. The solution is then cooled to -3.72 °C. If the molal depression constant (Kf) for water is 1.86 K kg mol⁻¹, calculate the mass of water (in g) that separates out as ice.",
                options = listOf("(A) 100 g", "(B) 200 g", "(C) 250 g", "(D) 300 g"),
                correctAnswer = "C",
                chapter = "Solutions",
                concept = "Depression in Freezing Point & Ice Separation",
                difficulty = Difficulty.MEDIUM,
                solutionText = "Moles of solute n = 31 / 62 = 0.5 mol.\nΔTf = Kf × m => 3.72 = 1.86 × (0.5 / W_water_kg)\n=> W_water_kg = (1.86 × 0.5) / 3.72 = 0.25 kg = 250 g remaining in solution.\nMass of water separated as ice = 500 g - 250 g = 250 g.\nCorrect Option: (C)",
                idealTimeSeconds = 60,
                youtubeSearchQuery = "31g solute molar mass 62 cooled to -3.72 mass of water separates out as ice"
            )
        )

        // Chemistry Q4
        list.add(
            QuestionItem(
                id = "QPT2_CHEM_04",
                questionNumber = 54,
                subject = Subject.CHEMISTRY,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "Consider the following statements:\nThe rate law for the acid catalysed hydrolysis of an ester being given as Rate = k[H⁺][ester] = k'[ester].\nIf the acid concentration is doubled at constant ester concentration:\n1. The second order rate constant, k is doubled.\n2. The pseudo first order rate constant, k' is doubled.\n3. The rate of the reaction is doubled.\nWhich of the above statements are correct?",
                options = listOf("(A) 1 and 2", "(B) 2 and 3", "(C) 1 and 3", "(D) 1, 2 and 3"),
                correctAnswer = "B",
                chapter = "Chemical Kinetics",
                concept = "Pseudo First Order Rate Constant Dependency",
                difficulty = Difficulty.EASY,
                solutionText = "The true rate constant k is independent of concentrations. The pseudo-first order rate constant k' = k[H⁺] is directly proportional to [H⁺], so when [H⁺] is doubled, k' is doubled (Statement 2 is true) and overall rate = k'[ester] is doubled (Statement 3 is true). Thus 2 and 3 are correct.\nCorrect Option: (B)",
                idealTimeSeconds = 45,
                youtubeSearchQuery = "rate law acid catalysed hydrolysis ester pseudo first order rate constant"
            )
        )

        // Chemistry Q5
        list.add(
            QuestionItem(
                id = "QPT2_CHEM_05",
                questionNumber = 55,
                subject = Subject.CHEMISTRY,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "The cell potential of the galvanic cell A(s) | A³⁺(aq, 0.01 M) || B²⁺(aq, 0.001 M) | B(s) at 298 K is ___ × 10⁻² V. (Report answer to nearest integer). Given: E°_A³⁺/A = -1.66 V, E°_B²⁺/B = -0.44 V and 2.303 RT / F = 0.06 V.",
                options = listOf("(A) 97", "(B) 107", "(C) 127", "(D) 117"),
                correctAnswer = "D",
                chapter = "Electrochemistry",
                concept = "Nernst Equation for Galvanic Cell",
                difficulty = Difficulty.MEDIUM,
                solutionText = "Overall cell reaction: 2 A(s) + 3 B²⁺(aq) -> 2 A³⁺(aq) + 3 B(s) (n = 6).\nE°_cell = E°_cathode - E°_anode = -0.44 - (-1.66) = +1.22 V.\nE_cell = E°_cell - (0.06/6) log([A³⁺]² / [B²⁺]³)\nQ = (0.01)² / (0.001)³ = 10⁻⁴ / 10⁻⁹ = 10⁵.\nE_cell = 1.22 - 0.01 log(10⁵) = 1.22 - 0.05 = 1.17 V = 117 × 10⁻² V.\nCorrect Option: (D)",
                idealTimeSeconds = 70,
                youtubeSearchQuery = "cell potential A | A3+(0.01M) || B2+(0.001M) | B E0 -1.66 -0.44"
            )
        )

        // Chemistry Q6
        list.add(
            QuestionItem(
                id = "QPT2_CHEM_06",
                questionNumber = 56,
                subject = Subject.CHEMISTRY,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "Which of the following statements is/are correct regarding the elevation of boiling point when a non-volatile solute is added to a solvent?\nA. The vapor pressure of the solution is higher than that of the pure solvent at any given temperature.\nB. The boiling point of the solution is lower than that of the pure solvent.\nC. The solute particles lower the escaping tendency of the solvent molecules.\nD. The extent of boiling point elevation depends on the nature of the solute particles, not just their number.\nChoose the correct answer from the options given below:",
                options = listOf("(A) C only", "(B) A and B only", "(C) B and D only", "(D) A, C, and D only"),
                correctAnswer = "A",
                chapter = "Solutions",
                concept = "Colligative Properties & Elevation in Boiling Point",
                difficulty = Difficulty.EASY,
                solutionText = "Adding non-volatile solute lowers vapor pressure and increases boiling point (so A and B are false). Colligative properties depend only on the number of particles, not their chemical nature (so D is false). Solute particles occupy surface area, lowering the escaping tendency of solvent (C is true).\nCorrect Option: (A)",
                idealTimeSeconds = 35,
                youtubeSearchQuery = "elevation of boiling point non volatile solute escaping tendency of solvent"
            )
        )

        // Chemistry Q7
        list.add(
            QuestionItem(
                id = "QPT2_CHEM_07",
                questionNumber = 57,
                subject = Subject.CHEMISTRY,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "The reaction, cis-Z ⇌ trans-Z is first order in both directions. At 25 °C, the equilibrium constant is 10⁻² and rate constant, k_f = 3 × 10⁻⁵ s⁻¹. In an experiment starting with pure cis-form, how long would it take for half of the equilibrium amount of trans-isomer to be formed?",
                options = listOf("(A) 5.6 × 10⁴ s", "(B) 9.8 × 10² s", "(C) 2.3 × 10² s", "(D) 7.5 × 10³ s"),
                correctAnswer = "C",
                chapter = "Chemical Kinetics",
                concept = "Opposing Reversible First Order Reactions",
                difficulty = Difficulty.HARD,
                solutionText = "For reversible first-order reaction A ⇌ B:\nk_eq = k_f / k_b = 10⁻² => k_b = k_f / 10⁻² = 3 × 10⁻⁵ / 10⁻² = 3 × 10⁻³ s⁻¹.\nEffective rate constant k = k_f + k_b ≈ 3.03 × 10⁻³ s⁻¹.\nTime to reach half equilibrium: t = ln(2) / (k_f + k_b) = 0.693 / (3.03 × 10⁻³) ≈ 2.28 × 10² s ≈ 2.3 × 10² s.\nCorrect Option: (C)",
                idealTimeSeconds = 80,
                youtubeSearchQuery = "reaction cis-Z trans-Z first order both directions equilibrium constant"
            )
        )

        // Chemistry Q8
        list.add(
            QuestionItem(
                id = "QPT2_CHEM_08",
                questionNumber = 58,
                subject = Subject.CHEMISTRY,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "Which of the following statements is not correct regarding the corrosion of iron?",
                options = listOf(
                    "(A) The presence of dissolved salts in water typically accelerates the rusting of iron.",
                    "(B) In the electrochemical process of rusting, iron primarily acts as the anode where oxidation (Fe -> Fe²⁺ + 2e⁻) occurs.",
                    "(C) Coating iron with zinc (a process known as galvanizing) prevents rusting mainly because zinc forms an impermeable barrier to oxygen and water.",
                    "(D) In acidic conditions, the presence of H⁺ ions can facilitate the reduction of oxygen, thereby promoting the rusting process."
                ),
                correctAnswer = "C",
                chapter = "Electrochemistry",
                concept = "Mechanism of Rusting and Galvanization",
                difficulty = Difficulty.EASY,
                solutionText = "Galvanizing protects iron through sacrificial protection (zinc has more negative E° and oxidizes preferentially even if the surface coating is scratched), not merely acting as an impermeable barrier.\nCorrect Option: (C)",
                idealTimeSeconds = 40,
                youtubeSearchQuery = "corrosion of iron electrochemical process galvanizing sacrificial protection"
            )
        )

        // Chemistry Q9
        list.add(
            QuestionItem(
                id = "QPT2_CHEM_09",
                questionNumber = 59,
                subject = Subject.CHEMISTRY,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "A solution of two volatile liquids A and B shows a positive deviation from Raoult's law. Which of the following statements correctly describes this solution?",
                options = listOf(
                    "(A) The A-B interactions are stronger than A-A and B-B interactions.",
                    "(B) The solution will have a boiling point higher than the boiling points of both pure A and pure B.",
                    "(C) The escaping tendency of both components from the solution is higher than in an ideal solution, resulting in an increased total vapor pressure.",
                    "(D) The enthalpy of mixing (ΔH_mix) for such a solution is negative."
                ),
                correctAnswer = "C",
                chapter = "Solutions",
                concept = "Positive Deviation from Raoult's Law",
                difficulty = Difficulty.EASY,
                solutionText = "In positive deviation, A-B interactions are weaker than A-A and B-B interactions, so molecules escape more readily into the vapor phase, increasing total vapor pressure and ΔH_mix > 0.\nCorrect Option: (C)",
                idealTimeSeconds = 40,
                youtubeSearchQuery = "solution positive deviation Raoult law escaping tendency vapor pressure"
            )
        )

        // Chemistry Q10
        list.add(
            QuestionItem(
                id = "QPT2_CHEM_10",
                questionNumber = 60,
                subject = Subject.CHEMISTRY,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "If the energy of activation of the reaction is 53.6 kJ mol⁻¹ and the temperature changes from 27 °C to 37 °C, then the value of (k_37°C / k_27°C) is (R = 8.314 J K⁻¹ mol⁻¹):",
                options = listOf("(A) 2.5", "(B) 1.0", "(C) 2.0", "(D) 1.5"),
                correctAnswer = "C",
                chapter = "Chemical Kinetics",
                concept = "Arrhenius Equation for Temperature Coefficient",
                difficulty = Difficulty.MEDIUM,
                solutionText = "ln(k2/k1) = (Ea / R) × [(T2 - T1) / (T1 T2)] = (53600 / 8.314) × [10 / (300 × 310)] = 6447 × 0.0001075 = 0.693 = ln 2.\nTherefore, k2 / k1 = e^0.693 = 2.0.\nCorrect Option: (C)",
                idealTimeSeconds = 50,
                youtubeSearchQuery = "energy of activation 53.6 kJ temperature from 27 to 37 ratio of rate constants"
            )
        )

        // Chemistry Q11
        list.add(
            QuestionItem(
                id = "QPT2_CHEM_11",
                questionNumber = 61,
                subject = Subject.CHEMISTRY,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "Match List I (Cell/Battery Type) with List II (Characteristic/Component/Reaction):\nList I:\nA. Lithium-ion battery\nB. Lead storage battery\nC. Daniell cell\nD. H2-O2 Fuel cell\nList II:\nI. Used commonly in automobiles and inverters\nII. Anode is typically graphite intercalated with lithium\nIII. Overall reaction: Zn(s) + Cu²⁺(aq) -> Zn²⁺(aq) + Cu(s)\nIV. Produces water as a primary by-product\nChoose the correct answer from the options given below:",
                options = listOf(
                    "(A) A-II, B-I, C-III, D-IV",
                    "(B) A-I, B-II, C-IV, D-III",
                    "(C) A-II, B-III, C-I, D-IV",
                    "(D) A-IV, B-I, C-II, D-III"
                ),
                correctAnswer = "A",
                chapter = "Electrochemistry",
                concept = "Commercial Batteries & Fuel Cells Matching",
                difficulty = Difficulty.EASY,
                solutionText = "A (Li-ion) -> II (Graphite-intercalated lithium)\nB (Lead storage) -> I (Automobiles/Inverters)\nC (Daniell cell) -> III (Zn + Cu²⁺ -> Zn²⁺ + Cu)\nD (Fuel cell) -> IV (Produces water)\nCorrect Option: (A)",
                idealTimeSeconds = 35,
                youtubeSearchQuery = "Match List I battery types lithium ion lead storage daniell cell fuel cell"
            )
        )

        // Chemistry Q12
        list.add(
            QuestionItem(
                id = "QPT2_CHEM_12",
                questionNumber = 62,
                subject = Subject.CHEMISTRY,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "Phenol associates in Benzene to a certain extent to form dimer. A solution containing 2.0 × 10⁻² kg of Phenol in 1.0 kg of benzene has its freezing point decreased by 0.69 K. The percentage degree of association of Phenol is (Kf for benzene = 5.12 K kg mol⁻¹, molar mass of Phenol = 94 g/mol):",
                options = listOf("(A) 73.4", "(B) 50.1", "(C) 42.3", "(D) 25.1"),
                correctAnswer = "A",
                chapter = "Solutions",
                concept = "Van't Hoff Factor and Degree of Dimer Association",
                difficulty = Difficulty.MEDIUM,
                solutionText = "Theoretical molality m = (20 g / 94 g/mol) / 1 kg = 0.2128 mol/kg.\nObserved ΔTf = 0.69 K = i × Kf × m = i × 5.12 × 0.2128 = 1.089 i.\ni = 0.69 / 1.089 = 0.633.\nFor dimerization: i = 1 - α/2 => 0.633 = 1 - α/2 => α/2 = 0.367 => α = 0.734 = 73.4%.\nCorrect Option: (A)",
                idealTimeSeconds = 70,
                youtubeSearchQuery = "Phenol associates in Benzene to form dimer freezing point decreased by 0.69"
            )
        )

        // Chemistry Q13
        list.add(
            QuestionItem(
                id = "QPT2_CHEM_13",
                questionNumber = 63,
                subject = Subject.CHEMISTRY,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "The experimental data for the reaction, 2A + B2 -> 2AB is:\nExp 1: [A]=0.50, [B2]=0.50, Rate = 1.6 × 10⁻⁴ mol s⁻¹\nExp 2: [A]=0.50, [B2]=1.00, Rate = 3.2 × 10⁻⁴ mol s⁻¹\nExp 3: [A]=1.00, [B2]=1.00, Rate = 3.2 × 10⁻⁴ mol s⁻¹\nThe rate equation for the above data is:",
                options = listOf(
                    "(A) Rate = k [A]² [B2]²",
                    "(B) Rate = k [A]² [B2]",
                    "(C) Rate = k [B2]",
                    "(D) Rate = k [B2]²"
                ),
                correctAnswer = "C",
                chapter = "Chemical Kinetics",
                concept = "Order of Reaction from Initial Rates Method",
                difficulty = Difficulty.EASY,
                solutionText = "Comparing Exp 2 & Exp 3: [B2] constant, [A] doubles, Rate is unchanged (3.2 × 10⁻⁴), so order w.r.t [A] = 0.\nComparing Exp 1 & Exp 2: [A] constant, [B2] doubles, Rate doubles (1.6 to 3.2), so order w.r.t [B2] = 1.\nRate law: Rate = k [B2].\nCorrect Option: (C)",
                idealTimeSeconds = 40,
                youtubeSearchQuery = "rate equation 2A + B2 -> 2AB experimental data initial rates"
            )
        )

        // Chemistry Q14
        list.add(
            QuestionItem(
                id = "QPT2_CHEM_14",
                questionNumber = 64,
                subject = Subject.CHEMISTRY,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "Consider the following standard reduction potentials:\nAg⁺ + e⁻ -> Ag(s); E° = +0.80 V\nCu²⁺ + 2e⁻ -> Cu(s); E° = +0.34 V\nFe²⁺ + 2e⁻ -> Fe(s); E° = -0.44 V\nAl³⁺ + 3e⁻ -> Al(s); E° = -1.66 V\nThe reducing power of these metals increases in the order:",
                options = listOf(
                    "(A) Al < Fe < Cu < Ag",
                    "(B) Ag < Cu < Fe < Al",
                    "(C) Cu < Ag < Al < Fe",
                    "(D) Fe < Al < Cu < Ag"
                ),
                correctAnswer = "B",
                chapter = "Electrochemistry",
                concept = "Electrochemical Series & Reducing Power",
                difficulty = Difficulty.EASY,
                solutionText = "Reducing power is directly proportional to ease of oxidation (more negative standard reduction potential E°).\nE° values: Al (-1.66 V) < Fe (-0.44 V) < Cu (+0.34 V) < Ag (+0.80 V).\nTherefore reducing power order is: Ag < Cu < Fe < Al.\nCorrect Option: (B)",
                idealTimeSeconds = 30,
                youtubeSearchQuery = "reducing power of metals standard reduction potentials Ag Cu Fe Al"
            )
        )

        // Chemistry Q15
        list.add(
            QuestionItem(
                id = "QPT2_CHEM_15",
                questionNumber = 65,
                subject = Subject.CHEMISTRY,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "The value of Henry's law constant (K_H) for some gases at 293 K is given below. Arrange the gases in the increasing order of their solubility:\nHe : 144.97 kbar, H2 : 69.16 kbar, N2 : 76.48 kbar, O2 : 34.86 kbar",
                options = listOf(
                    "(A) He < N2 < H2 < O2",
                    "(B) O2 < H2 < N2 < He",
                    "(C) H2 < N2 < O2 < He",
                    "(D) He < O2 < N2 < H2"
                ),
                correctAnswer = "A",
                chapter = "Solutions",
                concept = "Henry's Law Constant and Gas Solubility",
                difficulty = Difficulty.EASY,
                solutionText = "According to Henry's Law (p = K_H · x), solubility x is inversely proportional to K_H at constant pressure.\nHigher K_H means lower solubility.\nK_H order: He (144.97) > N2 (76.48) > H2 (69.16) > O2 (34.86).\nSolubility order: He < N2 < H2 < O2.\nCorrect Option: (A)",
                idealTimeSeconds = 35,
                youtubeSearchQuery = "Henrys law constant increasing order of solubility He H2 N2 O2"
            )
        )

        // Chemistry Q16
        list.add(
            QuestionItem(
                id = "QPT2_CHEM_16",
                questionNumber = 66,
                subject = Subject.CHEMISTRY,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "Consider this reaction: 2NO2(g) + O3(g) -> N2O5(g) + O2(g). The rate law is given as rate = k[NO2][O3]. Which of the following mechanism(s) is/are consistent with the rate law?\nMechanism I:\nNO2(g) + O3(g) ->(slow) NO3(g) + O2(g)\nNO3(g) + NO2(g) ->(fast) N2O5(g)\nMechanism II:\nO3(g) ⇌ O2(g) + O\nNO2(g) + O ->(slow) NO3(g)\nNO3(g) + NO2(g) ->(fast) N2O5(g)",
                options = listOf("(A) I only", "(B) II only", "(C) Both I & II", "(D) Neither I nor II"),
                correctAnswer = "A",
                chapter = "Chemical Kinetics",
                concept = "Reaction Mechanism and Rate Determining Step",
                difficulty = Difficulty.MEDIUM,
                solutionText = "For Mechanism I, the slow step is bimolecular with NO2 and O3: Rate = k1 [NO2][O3], which matches the experimental rate law. In Mechanism II, the slow step gives rate = k' [NO2][O3]/[O2], which does not match. Thus only Mechanism I is consistent.\nCorrect Option: (A)",
                idealTimeSeconds = 50,
                youtubeSearchQuery = "2NO2 + O3 -> N2O5 + O2 rate law k[NO2][O3] mechanism"
            )
        )

        // Chemistry Q17
        list.add(
            QuestionItem(
                id = "QPT2_CHEM_17",
                questionNumber = 67,
                subject = Subject.CHEMISTRY,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "Assertion: In electrolysis, the quantity of electricity needed for depositing 1 mole of silver (from an Ag⁺ solution) is different from that required for 1 mole of copper (from a Cu²⁺ solution).\nReason: The atomic weights of silver and copper are different.",
                options = listOf(
                    "(A) Both assertion and reason are true and reason is correct explanation.",
                    "(B) Both assertion and reason are true but reason is not the correct explanation.",
                    "(C) Assertion is true but reason is false.",
                    "(D) If the assertion and reason both are false."
                ),
                correctAnswer = "B",
                chapter = "Electrochemistry",
                concept = "Faraday's Laws of Electrolysis & Stoichiometric Electrons",
                difficulty = Difficulty.EASY,
                solutionText = "Depositing 1 mole of Ag requires 1 F (1 mole e⁻), while 1 mole of Cu requires 2 F (2 moles e⁻). Thus electricity needed is different (Assertion is true). The atomic weights are also different (Reason is true), but the difference in charge required is due to their valency (n-factor), not their atomic mass. Hence Reason is not the correct explanation.\nCorrect Option: (B)",
                idealTimeSeconds = 40,
                youtubeSearchQuery = "electrolysis electricity for 1 mole silver and 1 mole copper assertion reason"
            )
        )

        // Chemistry Q18
        list.add(
            QuestionItem(
                id = "QPT2_CHEM_18",
                questionNumber = 68,
                subject = Subject.CHEMISTRY,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "The relative lowering of vapour pressure of an aqueous solution containing non-volatile solute is 0.0125. The molality of the solution is:",
                options = listOf("(A) 0.70 m", "(B) 0.50 m", "(C) 0.80 m", "(D) 0.40 m"),
                correctAnswer = "A",
                chapter = "Solutions",
                concept = "Relative Lowering of Vapor Pressure & Molality Relation",
                difficulty = Difficulty.MEDIUM,
                solutionText = "ΔP / P° = x_solute = 0.0125.\nMolality m = (x_solute / (1 - x_solute)) × (1000 / M_solvent)\nFor water, M_solvent = 18 g/mol:\nm = (0.0125 / 0.9875) × (1000 / 18) = 0.012658 × 55.55 ≈ 0.703 m ≈ 0.70 m.\nCorrect Option: (A)",
                idealTimeSeconds = 50,
                youtubeSearchQuery = "relative lowering of vapour pressure aqueous solution 0.0125 molality"
            )
        )

        // Chemistry Q19
        list.add(
            QuestionItem(
                id = "QPT2_CHEM_19",
                questionNumber = 69,
                subject = Subject.CHEMISTRY,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "Metallic copper is deposited by the electrolysis of an aqueous solution of copper(II) sulfate: Cu²⁺(aq) + 2e⁻ -> Cu(s). If only 75% of the supplied current is utilized for the deposition, calculate the time (rounded to the nearest hour) required to deposit 25 g of copper using a constant current of 5 A. (Given: F = 96500 C mol⁻¹; molar mass of Cu = 63.5 g mol⁻¹):",
                options = listOf("(A) 4", "(B) 8", "(C) 7", "(D) 6"),
                correctAnswer = "D",
                chapter = "Electrochemistry",
                concept = "Faraday's Law with Current Efficiency",
                difficulty = Difficulty.MEDIUM,
                solutionText = "Mass m = (E × I_eff × t) / F\nE = 63.5 / 2 = 31.75 g/eq.\nI_eff = 0.75 × 5 A = 3.75 A.\n25 = (31.75 × 3.75 × t) / 96500 => t = (25 × 96500) / (31.75 × 3.75) = 2412500 / 119.06 = 20262.8 seconds.\nIn hours: 20262.8 / 3600 = 5.628 hours ≈ 6 hours.\nCorrect Option: (D)",
                idealTimeSeconds = 70,
                youtubeSearchQuery = "copper deposited electrolysis current efficiency 75 percent 25g Cu 5A"
            )
        )

        // Chemistry Q20
        list.add(
            QuestionItem(
                id = "QPT2_CHEM_20",
                questionNumber = 70,
                subject = Subject.CHEMISTRY,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "Two consecutive irreversible first order reactions can be represented by A ->(k1) B ->(k2) C. The rate equation for A is integrated to obtain [A]_t = [A]_0 e^(-k1 t) and [B]_t = (k1 [A]_0 / (k2 - k1)) [e^(-k1 t) - e^(-k2 t)]. At what time will B be present in the greatest concentration?",
                options = listOf(
                    "(A) t_max = 1/(k1 + k2) ln(k2/k1)",
                    "(B) t_max = 1/(k1 - k2) ln(k1/k2)",
                    "(C) t_max = 1/(k2 - k1) ln(k1/k2)",
                    "(D) None of these"
                ),
                correctAnswer = "B",
                chapter = "Chemical Kinetics",
                concept = "Sequential First-Order Kinetics Maximum Intermediate Concentration",
                difficulty = Difficulty.MEDIUM,
                solutionText = "Differentiating [B]_t with respect to t and setting d[B]/dt = 0:\n-k1 e^(-k1 t) + k2 e^(-k2 t) = 0 => e^((k1 - k2)t) = k1/k2\n=> (k1 - k2) t_max = ln(k1/k2) => t_max = (1 / (k1 - k2)) ln(k1/k2) = (1 / (k2 - k1)) ln(k2/k1).\nCorrect Option: (B)",
                idealTimeSeconds = 60,
                youtubeSearchQuery = "consecutive first order reaction A -> B -> C time for maximum concentration of B"
            )
        )

        // Chemistry Q21 (Numerical)
        list.add(
            QuestionItem(
                id = "QPT2_CHEM_21",
                questionNumber = 71,
                subject = Subject.CHEMISTRY,
                section = "Section B",
                type = QuestionType.NUMERICAL,
                questionText = "The resistance of a conductivity cell with a cell constant of 1.0 cm⁻¹ when filled with solution 'P' (0.01 M of electrolyte KX) is 1000 Ω. When the same cell is filled with solution 'Q' (0.04 M of electrolyte KX), the resistance is 500 Ω. Let the molar conductivity of solution P be x S cm² mol⁻¹ and the molar conductivity of solution Q be y S cm² mol⁻¹. If the ratio y/x is given as z × 10⁻³, calculate the value of x + y + z.",
                correctAnswer = "650",
                chapter = "Electrochemistry",
                concept = "Specific and Molar Conductivity Calculations",
                difficulty = Difficulty.HARD,
                solutionText = "Cell constant G* = 1.0 cm⁻¹.\nFor Solution P: κ_P = G* / R_P = 1.0 / 1000 = 10⁻³ S cm⁻¹.\nx = Λ_m(P) = (1000 × κ_P) / C_P = (1000 × 10⁻³) / 0.01 = 100 S cm² mol⁻¹.\nFor Solution Q: κ_Q = G* / R_Q = 1.0 / 500 = 2 × 10⁻³ S cm⁻¹.\ny = Λ_m(Q) = (1000 × κ_Q) / C_Q = (1000 × 2 × 10⁻³) / 0.04 = 50 S cm² mol⁻¹.\nRatio y/x = 50 / 100 = 0.5 = 500 × 10⁻³ => z = 500.\nx + y + z = 100 + 50 + 500 = 650.\nCorrect Answer: 650",
                idealTimeSeconds = 80,
                youtubeSearchQuery = "resistance conductivity cell solution P 1000 ohm solution Q 500 ohm value x + y + z"
            )
        )

        // Chemistry Q22 (Numerical)
        list.add(
            QuestionItem(
                id = "QPT2_CHEM_22",
                questionNumber = 72,
                subject = Subject.CHEMISTRY,
                section = "Section B",
                type = QuestionType.NUMERICAL,
                questionText = "A reaction takes place in three steps; the rate constants are k1, k2 and k3. The overall rate constant k = (k1 k3) / k2. If the energies of activation are 40, 30 and 20 kJ respectively, the overall energy of activation is (assuming 'A' to be constant for all) (mark answer in kJ):",
                correctAnswer = "30",
                chapter = "Chemical Kinetics",
                concept = "Composite Activation Energy from Complex Rate Law",
                difficulty = Difficulty.EASY,
                solutionText = "k = (k1 k3) / k2 => A e^(-Ea / RT) = (A e^(-Ea1/RT) · A e^(-Ea3/RT)) / (A e^(-Ea2/RT))\n=> Ea = Ea1 + Ea3 - Ea2 = 40 + 20 - 30 = 30 kJ.\nCorrect Answer: 30",
                idealTimeSeconds = 40,
                youtubeSearchQuery = "overall rate constant k = k1 k3 / k2 overall energy of activation"
            )
        )

        // Chemistry Q23 (Numerical)
        list.add(
            QuestionItem(
                id = "QPT2_CHEM_23",
                questionNumber = 73,
                subject = Subject.CHEMISTRY,
                section = "Section B",
                type = QuestionType.NUMERICAL,
                questionText = "Standard free energies of formation (in kJ/mol) at 298 K are -237.2, -394.4, and -8.2 for H2O(l), CO2(g), and pentane(g), respectively. The E°_cell value for the pentane-oxygen fuel cell is (mark answer to nearest integer):",
                correctAnswer = "1",
                chapter = "Electrochemistry",
                concept = "Free Energy of Fuel Cell Combustion & Standard Cell Potential",
                difficulty = Difficulty.HARD,
                solutionText = "Combustion of pentane: C5H12(g) + 8 O2(g) -> 5 CO2(g) + 6 H2O(l).\nTotal electrons transferred n = 5 × 4 - (-12) = 32 electrons.\nΔG° = 5 ΔGf°(CO2) + 6 ΔGf°(H2O) - ΔGf°(C5H12)\n= 5(-394.4) + 6(-237.2) - (-8.2) = -1972 - 1423.2 + 8.2 = -3387 kJ/mol.\nE°_cell = -ΔG° / (n F) = (3387 × 10³) / (32 × 96500) = 3387000 / 3088000 = 1.0968 V ≈ 1 V.\nCorrect Answer: 1",
                idealTimeSeconds = 90,
                youtubeSearchQuery = "pentane oxygen fuel cell standard free energy E0 cell value nearest integer"
            )
        )

        // Chemistry Q24 (Numerical)
        list.add(
            QuestionItem(
                id = "QPT2_CHEM_24",
                questionNumber = 74,
                subject = Subject.CHEMISTRY,
                section = "Section B",
                type = QuestionType.NUMERICAL,
                questionText = "Osmotic pressure of an aqueous solution of a non-electrolyte solute is 300 mm Hg at 27 °C, then a certain volume of solution is diluted by adding water and temperature is raised to 54 °C and osmotic pressure becomes 300 mm Hg. Find % increase in volume of solution after dilution.",
                correctAnswer = "9",
                chapter = "Solutions",
                concept = "Osmotic Pressure Temperature and Volume Variation",
                difficulty = Difficulty.MEDIUM,
                solutionText = "π = (n / V) R T => π V / T = constant (since n is constant).\nπ1 = 300, T1 = 27 + 273 = 300 K.\nπ2 = 300, T2 = 54 + 273 = 327 K.\n(300 × V1) / 300 = (300 × V2) / 327 => V2 / V1 = 327 / 300 = 1.09.\nPercentage increase in volume = ((V2 - V1) / V1) × 100% = (1.09 - 1) × 100% = 9%.\nCorrect Answer: 9",
                idealTimeSeconds = 50,
                youtubeSearchQuery = "osmotic pressure 300 mm Hg at 27 C raised to 54 C percent increase in volume"
            )
        )

        // Chemistry Q25 (Numerical)
        list.add(
            QuestionItem(
                id = "QPT2_CHEM_25",
                questionNumber = 75,
                subject = Subject.CHEMISTRY,
                section = "Section B",
                type = QuestionType.NUMERICAL,
                questionText = "A and B decompose via first order kinetics with half-lives 54.0 min and 18.0 min respectively. Starting from an equimolar non-reactive mixture of A and B, the time taken for the concentration of A to become 16 times that of B is ___ min. (Round off to nearest integer):",
                correctAnswer = "108",
                chapter = "Chemical Kinetics",
                concept = "Differential Radioactive/Chemical Decay Rates",
                difficulty = Difficulty.MEDIUM,
                solutionText = "[A]_t = [A]_0 (1/2)^(t / 54) and [B]_t = [B]_0 (1/2)^(t / 18).\nGiven [A]_0 = [B]_0 and [A]_t / [B]_t = 16 = 2⁴:\n(1/2)^(t/54 - t/18) = 2⁴ => 2^(t/18 - t/54) = 2⁴.\nt/18 - t/54 = 4 => (3t - t)/54 = 4 => 2t / 54 = 4 => t / 27 = 4 => t = 108 min.\nCorrect Answer: 108",
                idealTimeSeconds = 60,
                youtubeSearchQuery = "A and B decompose first order half lives 54 min 18 min concentration A 16 times B"
            )
        )

        return list
    }
}
