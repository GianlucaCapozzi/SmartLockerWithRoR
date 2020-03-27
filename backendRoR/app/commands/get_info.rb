class GetInfo
    prepend SimpleCommand
  
    def initialize(headers = {})
        @headers = headers
    end
  
    def call
        user if AuthorizeApiRequest.call(headers)
    end

    private

    attr_accessor :headers
  
    def user
        @user ||= User.find(decoded_auth_token[:user_id]) if decoded_auth_token
    end

    def decoded_auth_token
        @decoded_auth_token ||= JsonWebToken::decode(http_auth_header)
    end
  
    def http_auth_header

        if headers['Authorization'].present?
            return headers['Authorization'].split(' ').last
        else
            errors.add(:token, 'Missing token')
        end
        nil
    end
end