docker run -d --name rabbit_taekwondo \
  -e RABBITMQ_DEFAULT_USER=root \
  -e RABBITMQ_DEFAULT_PASS=31102005 \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:3-management

# B1: chuột phải -> chọn Open git bash here
# B2: chmod +x rabbitmq.sh
# B3: ./rabbitmq.sh

# Lưu ý khi làm việc với git bash không dùng được Ctrl_C Ctrl_V, phải copy bằng chuột