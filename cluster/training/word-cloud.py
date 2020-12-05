import numpy as np
from PIL import Image
from wordcloud import WordCloud

# read file
f = open("C:\\Users\\Melih\\Senior Project\\test-parse.txt", "r", encoding="utf-8")
words = ''
words = words.join(f.read().split("\n"))
f.close()
# read file
f = open("stop-words2.tr.txt", "r", encoding="utf-8")
stopwords = f.read().split("\n")
f.close()

mask = np.array(Image.open("mask-cloud.png"))

wordcloud1 = WordCloud(width = 600, height = 600, 
                background_color ='white',
                stopwords = stopwords,
                min_font_size = 10).generate(words); 

wordcloud2 = WordCloud(width = 600, height = 600, 
                background_color ='white',
                min_font_size = 10).generate(words)

wordcloud2.to_file("C:\\Users\\Melih\\Desktop\\Dersler\\Data Mining\\Project\\Final Report\\original_word_cloud.png")
wordcloud1.to_file("C:\\Users\\Melih\\Desktop\\Dersler\\Data Mining\\Project\\Final Report\\stop_word_removed_word_cloud.png")